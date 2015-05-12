package edu.uccs.cs5920.broker;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

public class TokenProvider {

	private RsaJsonWebKey jwk;
	public TokenProvider() throws Exception {
		 jwk = new RsaJsonWebKey(readKeyFromFile("public.key"));
		 jwk.setPrivateKey(readPrivKeyFromFile("private.key"));
	}
	
	
	public String generateToken(String uuid) throws Exception {
		// Create the Claims, which will be the content of the JWT
		JwtClaims claims = new JwtClaims();
		claims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
		claims.setGeneratedJwtId(); 
		claims.setJwtId(uuid);
		JsonWebSignature jws = new JsonWebSignature();
		
		// The payload of the JWS is JSON content of the JWT Claims
		jws.setPayload(claims.toJson());
		
		// The JWT is signed using the private key
		jws.setKey(jwk.getPrivateKey());		
		jws.setKeyIdHeaderValue(jwk.getKeyId());
		
		// Set the signature algorithm on the JWT/JWS that will integrity protect the claims
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
		
		return jws.getCompactSerialization();		
	}
	
	public RSAPublicKey readKeyFromFile(String keyFileName) throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(keyFileName);
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPublicKey pubKey = (RSAPublicKey)fact.generatePublic(keySpec);
			return pubKey;
		} catch (Exception e) {
			throw new RuntimeException("Spurious serialisation error", e);
		} finally {
			oin.close();
		}
	}

	public PrivateKey readPrivKeyFromFile(String keyFileName) throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(keyFileName);
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey pubKey = fact.generatePrivate(keySpec);
			return pubKey;
		} catch (Exception e) {
			throw new RuntimeException("Spurious serialisation error", e);
		} finally {
			oin.close();
		}
	}
}
