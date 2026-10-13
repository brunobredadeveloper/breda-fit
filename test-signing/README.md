# Public test fixture — NOT production credentials

fit-debug.keystore is intentionally public, generated solely for internal debug builds. Alias androiddebugkey, passwords android. It has no relationship to production accounts or production signing keys.

Debug applicationId is dev.breda.fit.debug. Local and CI debug APKs use this same key so internal updates preserve test data. Release uses the separate dev.breda.fit ID and must be signed with the publisher's private production key. Do not treat debug builds as secure production distribution, and use fictitious data for evaluation. Never reuse this key for production.
