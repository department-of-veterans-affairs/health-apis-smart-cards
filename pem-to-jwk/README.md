# PEM to JWK for Smart Cards

This utility converts a `.pem` certificate to a JWK in accordance to the [Smart Health Cards Framework](https://smarthealth.cards/#generating-and-resolving-cryptographic-keys)

The JWK is used to sign the Verifiable Credential payload.

The SSL keys must be of `EC` kind (NOT the usual `RSA`), using the `ES256` algorithm with a `P-256` elliptic curve.

A valid certificate should then be generated from the SSL keys. For lower environments, the certificate can be *self-signed*. For higher environments (sandbox and production), these must be **VA-signed** and provided by Lighthouse Devops.

The `pem2jwk.sh` utility can be used to convert the provided certificate `.pem` to its `jwk` representation.

## Contents:
1. [Generating a self-signed certificates for internal testing](#Generating-a-self-signed-certificates-for-internal-testing)
2. [Generating VA-signed certificates for production](#Generating-VA-signed-certificates-for-production)
3. [Convert PEM to JWK](#Convert-PEM-to-JWK)

---

## Generating a self-signed certificates for internal testing

Use `openssl` to generate the private and public keys, and issue a self-signed certificate.

Replace file names with whatever you desire.

1. Create private key with `EC` kind:
```
openssl ecparam -name prime256v1 -genkey -noout -out private-key.pem
```

2. Generate corresponding public key:
```
# generate corresponding public key
openssl ec -in private-key.pem -pubout -out public-key.pem
```

3. Issue self-signed certificate with 5-year expiration period:
```
openssl req -new -x509 -key private-key.pem -out cert.pem -days 1825
```

## Generating VA-signed certificates for production

Create a new request on Github [lighthouse-devops-support](https://github.com/department-of-veterans-affairs/lighthouse-devops-support/issues). Example previous request [here](https://github.com/department-of-veterans-affairs/lighthouse-devops-support/issues/183).

### `Method 1: provide parameters`

The request body should include the following information
```
[ ecparam ]
name = prime256v1
genkey = true
noout = true

[ dn ]
C = US
ST = DC
L = Washington
O = Department of Veterans Affairs
OU = DVP
CN = production.smart-cards.lighthouse.va.gov
```

### `Method 2: provide CSR`

Alternatively, you can provide the request in CSR format:

1. Create CSR file:

**Change `CN` value to environment domain**
```
winpty openssl ecparam -out production-smart-cards_lighthouse_va_gov.pkey -name prime256v1 -genkey && winpty openssl req -new -key production-smart-cards_lighthouse_va_gov.pkey -nodes -out production-smart-cards_lighthouse_va_gov.csr -subj '/C=US/ST=DC/L=Washington/O=DVP/OU=Department of Veterans Affairs/CN=production-smart-cards.lighthouse.va.gov'
```

If you're using *Git for Windows* (aka Git Bash), you must provide an extra double slash value at the beginning of the `-subj` value, to suppress a slash translation problem:

```
winpty openssl ecparam -out production-smart-cards_lighthouse_va_gov.pkey -name prime256v1 -genkey && winpty openssl req -new -key production-smart-cards_lighthouse_va_gov.pkey -nodes -out production-smart-cards_lighthouse_va_gov.csr -subj '//C=US/C=US/ST=DC/L=Washington/O=DVP/OU=Department of Veterans Affairs/CN=production-smart-cards.lighthouse.va.gov'
```

2. Verify output CSR:
```
openssl req -text -in your-output-file.csr -noout -verify
```
Look for `verify OK` and `Subject: ` fields in output:
```
(some values truncated with ...)

Certificate Request:
    Data:
        Version: 1 (0x0)
        Subject: C = US, ST = DC, L = Washington, O = DVP, OU = Department of Veterans Affairs, CN = production-smart-cards.lighthouse.va.gov
        Subject Public Key Info:
            Public Key Algorithm: id-ecPublicKey
                Public-Key: (256 bit)
                pub:
                    ...
                ASN1 OID: prime256v1
                NIST CURVE: P-256
        Attributes:
            ...
    Signature Algorithm: ecdsa-with-SHA256
         ...
verify OK

```

3. Attach CSR file to DevOps request.

---

## Convert PEM to JWK

When a certificate `.pem` file is generated, use the `pem2jwk.sh` script to convert to JWK. The script outputs the JWK to stdout.

```
./pem2jwk /path/to/cert.pem
```

**CAREFUL**: If using the private key PEM as the input file, the util's output includes both private and public JWK representations. The private JWK should be protected and encrypted before storing.

Sample output:
```
From /path/to/cert.pem:
Type     : EC
Use      : null
Thumb    : <<Thumbprint in BASE64>>
To json  : {"kty":"EC","crv":"P-256","x":"...","y":"..."}
To Public: {"kty":"EC","crv":"P-256","x":"...","y":"..."}
Out json : {"kty":"EC","use":"sig","crv":"P-256","kid":"<<Thumbprint in BASE64>>","x":"...","y":"...","alg":"ES256"}
Out publ : {"kty":"EC","use":"sig","crv":"P-256","kid":"<<Thumbprint in BASE64>>","x":"...","y":"...","alg":"ES256"}

```

## Resources

Following guides were used to come up with this approach.
 - https://www.digicert.com/kb/ssl-support/openssl-quick-reference-guide.htm
 - https://www.scottbrady91.com/OpenSSL/Creating-Elliptical-Curve-Keys-using-OpenSSL
 - https://ruleoftech.com/2020/generating-jwt-and-jwk-for-information-exchange-between-services
 - https://connect2id.com/products/nimbus-jose-jwt

CSR Command Generator:
 - https://www.digicert.com/easy-csr/openssl.htm
