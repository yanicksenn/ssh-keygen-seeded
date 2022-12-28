#!/bin/zsh

read -s "seed?Seed: "
echo ""

read -s "passphrase?Passphrase: "
echo ""

read -s "confirm?Confirm: "
echo ""

if [ ! "$passphrase" = "$confirm" ]; then
	echo "Phasephrases did not match"
	exit 1
fi

target/ssh-keygen-seeded rsa \
	--seed=$seed \
	--passphrase=$passphrase \
	--key-size=2048 \
	--verbose

mv id_rsa ~/.ssh/
mv id_rsa.pub ~/.ssh/
echo "Moved keypair to ~/.ssh"

chmod 600 ~/.ssh/id_rsa
chmdo 600 ~/.ssh/id_rsa.pub
echo "Adjusted permissions"

echo "Validation"
ssh-keygen -y
