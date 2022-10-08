FROM ubuntu:22.10

USER root

# Install dependencies
RUN apt-get -y update
RUN apt-get -y install openssh-server
RUN apt-get -y install git

# Enable ssh
RUN systemctl enable ssh

# Create .ssh folder
RUN mkdir -p /root/.ssh

# Copy private key
COPY id_rsa /root/.ssh
RUN chmod 600 /root/.ssh/id_rsa

# Copy public key
COPY id_rsa.pub /root/.ssh
RUN chmod 600 /root/.ssh/id_rsa.pub