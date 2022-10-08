## Build requirements

- Maven
- GraalVM 22.2 / JDK 17 

## Build the tool

```bash
mvn clean install -P native
```

## Using the tool

```bash
# Generating RSA key pair at the current working directory
ssh-keygen-seeded --seed=MY_SEED --passphrase=MY_PASSPHRASE
```

```bash
# Generating RSA key pair with custom key-size (default is 4096)
ssh-keygen-seeded --seed=MY_SEED --passphrase=MY_PASSPHRASE --key-size=2048
ssh-keygen-seeded --seed=MY_SEED --passphrase=MY_PASSPHRASE --key-size=1024
```

```bash
# Generating additional output of the tool 
ssh-keygen-seeded --seed=MY_SEED --passphrase=MY_PASSPHRASE -v 
ssh-keygen-seeded --seed=MY_SEED --passphrase=MY_PASSPHRASE --verbose 
```

```bash
# Print help
ssh-keygen-seeded -h
ssh-keygen-seeded --help
```


## Disclaimer
This tool does not really follow any security standards and should probably 
not be used in a productive environment. Use at your own risk.