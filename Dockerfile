FROM nexus-repo.tntad.fedex.com/qcoe/owasp/zap2docker-stable:2.9.0
COPY zapcontext .
WORKDIR .
