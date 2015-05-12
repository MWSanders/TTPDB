package edu.uccs.cs5920.common;

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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

public class TokenManager {
	
	public static void main(String args[]) throws JoseException, Exception {
		TokenManager manager = new TokenManager();

		String token = manager.generateToken(UUID.randomUUID().toString());
		System.out.println("Token: " + token);
		String uuid = manager.validateToken(token);
		System.out.println("UUID: " + uuid);
		
//		RsaJsonWebKey jwk = new RsaJsonWebKey(manager.readKeyFromFile("public.key"));
//		jwk.setPrivateKey(manager.readPrivKeyFromFile("private.key"));
//
//		// Create the Claims, which will be the content of the JWT
//		JwtClaims claims = new JwtClaims();
//		claims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
//		claims.setGeneratedJwtId(); 
//		claims.setJwtId(UUID.randomUUID().toString());
//		JsonWebSignature jws = new JsonWebSignature();
//
//		// The payload of the JWS is JSON content of the JWT Claims
//		jws.setPayload(claims.toJson());
//
//		// The JWT is signed using the private key
//		jws.setKey(jwk.getPrivateKey());
//
//		jws.setKeyIdHeaderValue(jwk.getKeyId());
//
//		// Set the signature algorithm on the JWT/JWS that will integrity protect the claims
//		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
//
//		String jwt = jws.getCompactSerialization();
//
//
//		// Now you can do something with the JWT. Like send it to some other party
//		// over the clouds and through the interwebs.
//		System.out.println("JWT: " + jwt);
//
//
//		// decryption key resolver to the builder.
//		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
//		.setRequireExpirationTime() // the JWT must have an expiration time
//		.setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
//		.setVerificationKey(manager.readKeyFromFile("public.key")) // verify the signature with the public key
//		.build(); // create the JwtConsumer instance
//
//		try
//		{
//			//  Validate the JWT and process it to the Claims
//			JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
//			System.out.println("JWT validation succeeded! " + jwtClaims);
//		}
//		catch (InvalidJwtException e)
//		{
//			System.out.println("Invalid JWT! " + e);
//		}
	}
	
	public String generateToken(String uuid) throws Exception {
		RsaJsonWebKey jwk = new RsaJsonWebKey(readKeyFromFile("public.key"));
		jwk.setPrivateKey(readPrivKeyFromFile("private.key"));
		
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
		jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);
		
		return jws.getCompactSerialization();		
	}
	
	public String validateToken(String token) throws Exception {
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
		.setRequireExpirationTime() // the JWT must have an expiration time
		.setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
		.setVerificationKey(readKeyFromFile("public.key")) // verify the signature with the public key
		.build(); // create the JwtConsumer instance

		
		//  Validate the JWT and process it to the Claims
		JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
		System.out.println("JWT validation succeeded! " + jwtClaims);
		
		return jwtClaims.getJwtId();
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

