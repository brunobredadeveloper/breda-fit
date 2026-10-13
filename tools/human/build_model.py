#!/usr/bin/env python3
"""Reproducible CC0 asset conversion. Original converter; no MakeHuman application code.
Only asset files named in sources.json are fetched, hash checked and packaged offline.
"""
import argparse, collections, re, hashlib, json, math, pathlib, shutil, struct, urllib.request, zlib
ROOT=pathlib.Path(__file__).resolve().parents[2]
def fetch(cache):
    specs=json.loads((ROOT/'tools/human/sources.json').read_text())
    for name,s in specs.items():
        p=cache/name;p.parent.mkdir(parents=True,exist_ok=True)
        if not p.exists() or hashlib.sha256(p.read_bytes()).hexdigest()!=s['sha256']:
            req=urllib.request.Request(s['url'],headers={'Range':s['range']} if 'range' in s else {})
            with urllib.request.urlopen(req,timeout=90) as r:
                data=r.read()
                if 'range' in s:
                    assert r.status==206
                    if s.get('deflate'):data=zlib.decompress(data,-15)
            assert hashlib.sha256(data).hexdigest()==s['sha256'],name
            p.write_bytes(data)
    return cache

def obj(path):
    v=[];uv=[];faces=[];group='body'
    for line in path.read_text().splitlines():
        x=line.split()
        if not x:continue
        if x[0]=='v':v.append([float(t) for t in x[1:4]])
        elif x[0]=='vt':uv.append([float(t) for t in x[1:3]])
        elif x[0]=='g':group=x[1]
        elif x[0]=='f':
            pairs=[tuple(int(a)-1 for a in t.split('/')[:2]) for t in x[1:]]
            for i in range(1,len(pairs)-1):faces.append((group,[pairs[0],pairs[i],pairs[i+1]]))
    return v,uv,faces

def bone_id(n):
    side=0 if n.endswith('.L') else 3
    if 'upperarm' in n:return 4+side
    if 'lowerarm' in n:return 5+side
    if any(t in n for t in ('finger','wrist','metacarpal')):return 6+side
    if 'upperleg' in n:return 10+side
    if 'lowerleg' in n:return 11+side
    if 'foot' in n or 'toe' in n:return 12+side
    if any(t in n for t in ('head','neck','eye','jaw','levator','oculi','orbicularis','oris','risorius','tongue','special')):return 3
    if n in ('spine04','spine05'):return 1
    if 'spine' in n or 'breast' in n or 'clavicle' in n or 'shoulder' in n:return 2
    return 0

def normalise(w):
    vals=sorted(((k,max(0,v)) for k,v in w.items()),key=lambda x:-x[1])[:4]
    total=sum(v for k,v in vals)
    if total<1e-8:vals=[(0,1)];total=1
    vals=[(k,v/total) for k,v in vals]
    return vals+[(0,0)]*(4-len(vals))

def main(cache,out):
    base,uv,faces=obj(cache/'base.obj')
    for name,amount in [('male.target',1.),('muscle.target',.18)]:
        for line in (cache/name).read_text().splitlines():
            p=line.split()
            if len(p)==4 and not line.startswith('#'):
                i=int(p[0]);base[i]=[base[i][a]+float(p[a+1])*amount for a in range(3)]
    weight=[collections.defaultdict(float) for _ in base]
    for n,items in json.loads((cache/'weights.json').read_text())['weights'].items():
        for i,w in items:weight[i][bone_id(n)]+=w
    skeleton=json.loads((cache/'default.mhskel').read_text())
    def joint(b,end):
        ids=skeleton['joints'][skeleton['bones'][b][end]]
        return [sum(base[i][a] for i in ids)/len(ids) for a in range(3)]
    rest=[(joint('root','head'),joint('spine05','head')),
          (joint('spine05','head'),joint('spine03','head')),
          (joint('spine03','head'),joint('neck01','head')),
          (joint('head','head'),joint('head','tail'))]
    for s in ['L','R']:
        rest += [(joint('upperarm01.'+s,'head'),joint('upperarm02.'+s,'tail')),
                 (joint('lowerarm01.'+s,'head'),joint('lowerarm02.'+s,'tail')),
                 (joint('wrist.'+s,'head'),joint('wrist.'+s,'tail'))]
    for s in ['L','R']:
        rest += [(joint('upperleg01.'+s,'head'),joint('upperleg02.'+s,'tail')),
                 (joint('lowerleg01.'+s,'head'),joint('lowerleg02.'+s,'tail')),
                 (joint('foot.'+s,'head'),joint('foot.'+s,'tail'))]
    scale=.133; floor=-8.2
    def world(v):return [v[0]*scale,(v[1]-floor)*scale,v[2]*scale]
    out.mkdir(parents=True,exist_ok=True)
    delete=set();attachments=[]
    for name,mat in [('outfit',1),('shoes',4),('hair',3),('brows',3),('eyes',2)]:
        av,au,af=obj(cache/(name+'.obj'));mapping=[];inside=False;deleting=False
        ratios=[1.,1.,1.]
        for line in (cache/(name+'.mhclo')).read_text().splitlines():
            p=line.split()
            if not p or p[0].startswith('#'):continue
            if p[0] in ('x_scale','y_scale','z_scale'):
                axis={'x_scale':0,'y_scale':1,'z_scale':2}[p[0]];ratios[axis]=abs(base[int(p[1])][axis]-base[int(p[2])][axis])/float(p[3])
            elif p[0]=='verts':inside=True
            elif p[0]=='delete_verts':inside=False;deleting=True
            elif inside and p[0].lstrip("-").isdigit():
                if len(p)==1:mapping.append(([int(p[0])],[1.],[0.,0.,0.]))
                elif len(p)==9:mapping.append(([int(a) for a in p[:3]],[float(a) for a in p[3:6]],[float(a) for a in p[6:]]))
                else:inside=False
            elif deleting and name in ('outfit','shoes'):
                for first,last in re.findall(r"(\d+)(?:\s*-\s*(\d+))?",line):
                    delete.update(range(int(first),int(last or first)+1))
        assert len(mapping)==len(av),(name,len(mapping),len(av))
        aw=[]
        for k,(ids,ws,offset) in enumerate(mapping):
            av[k]=[sum(base[i][axis]*w for i,w in zip(ids,ws))+offset[axis]*ratios[axis] for axis in range(3)]
            merged=collections.defaultdict(float)
            for i,w in zip(ids,ws):
                for b,amount in weight[i].items():merged[b]+=amount*w
            aw.append(merged)
        attachments.append((name,mat,av,au,af,aw))
    meshes=[]
    # Clothing masks hide covered body vertices, including private anatomy.
    bf=[(g,f) for g,f in faces if g=='body' and not any(i in delete for i,u in f)]
    meshes.append(('skin',0,base,uv,bf,weight))
    meshes+=attachments
    def write_mesh(name,mat,vertices,uv,faces,weights):
        norms=[[0.,0.,0.] for _ in vertices]
        for g,f in faces:
            a,b,c=[vertices[i] for i,u in f];ab=[b[k]-a[k] for k in range(3)];ac=[c[k]-a[k] for k in range(3)];n=[ab[1]*ac[2]-ab[2]*ac[1],ab[2]*ac[0]-ab[0]*ac[2],ab[0]*ac[1]-ab[1]*ac[0]]
            for i,u in f:
                for k in range(3):norms[i][k]+=n[k]
        garment_top=set()
        if name=='outfit':
            adjacency=collections.defaultdict(set)
            for g,f in faces:
                for i,u in f:adjacency[i].update(j for j,v in f)
            seen=set()
            for i in range(len(vertices)):
                if i in seen:continue
                queue=[i];component=[];seen.add(i)
                while queue:
                    j=queue.pop();component.append(j)
                    for neighbour in adjacency[j]:
                        if neighbour not in seen:seen.add(neighbour);queue.append(neighbour)
                if max(vertices[j][1] for j in component)>5:garment_top.update(component)
        vals=[];indices=[];lookup={}
        for g,f in faces:
            for i,u in f:
                key=(i,u)
                if key not in lookup:
                    lookup[key]=len(vals)//17;v=world(vertices[i]);n=norms[i];l=math.sqrt(sum(t*t for t in n)) or 1;ws=normalise(weights[i]);
                    # Garment zones retain geometric folds, with our own sports palette (no source logos).
                    zone=1. if i in garment_top else 0.
                    vals.extend(v+[t/l for t in n]+uv[u]+[float(b) for b,w in ws]+[w for b,w in ws]+[zone])
                indices.append(lookup[key])
        assert len(lookup)<65536
        with (out/(name+'.mesh')).open('wb') as f:
            f.write(struct.pack('<4I',0x34484642,mat,len(lookup),len(indices)));f.write(struct.pack('<%df'%len(vals),*vals));f.write(struct.pack('<%dH'%len(indices),*indices))
        print(name,len(lookup),len(indices)//3)
    for args in meshes:write_mesh(*args)
    with (out/'rig.bin').open('wb') as f:
        for a,b in rest:f.write(struct.pack('<6f',*(world(a)+world(b))))
    for src,dest in [('skin.png','skin.png'),('hair.png','hair.png'),('brows.png','brows.png'),('eyes.png','eyes.png'),('shoes.png','shoes.png')]:shutil.copyfile(cache/src,out/dest)
    shutil.copyfile(ROOT/'tools/human/NOTICE.md',out/'NOTICE.md')
    print('Output:',out)
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--cache',type=pathlib.Path,default=ROOT/'.human-cache');p.add_argument('--output',type=pathlib.Path,default=ROOT/'androidApp/src/main/assets/human');args=p.parse_args();main(fetch(args.cache),args.output)
