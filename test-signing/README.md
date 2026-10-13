# Evaluation signing

GitHub Actions generates an ephemeral Android debug signing key on the runner. No key is versioned or distributed with source. Different runs can have different certificates.

The visual preview uses dev.breda.fit.preview02 to coexist with the older debug app. There is no automatic profile/history migration between these application IDs. Commercial publication and reliable upgrades require securely provisioned, publisher-owned signing credentials.
