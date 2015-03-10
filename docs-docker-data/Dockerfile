FROM busybox
VOLUME ["/var/docs"]
ADD data /var/docs/
RUN chown -R 1001:1001 /var/docs
CMD ["/bin/sh"]
