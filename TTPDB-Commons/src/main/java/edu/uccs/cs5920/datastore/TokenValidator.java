package edu.uccs.cs5920.datastore;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.Base64.Decoder;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;



public class TokenValidator {
	Decoder decoder = Base64.getDecoder();
	Mongo mongoClient;
	DB db;
	Jongo jongo;
	MongoCollection docCollection;
	RSAPublicKey publicKey;

	public static void main(String[] args) throws Exception {
//		TokenValidator manager = new TokenValidator();
//
//		//String token = manager.generateToken(UUID.randomUUID().toString());
//		String token ="eyJraWQiOm51bGwsImFsZyI6IlJTMjU2In0.eyJleHAiOjE0Mjc5MjUzODgsImp0aSI6ImZiZDdhOWNjLTdlMDItNDAxMi04ZTEzLTJhYTZjNDA0YmJlMyJ9.Ojaw_S0zn6PizgMSl9J1KOk3sKqoYiJf4GoDuhAUkdNjGiDgZbUgg36WDdPah2EpGZm_av3JUZnr8FOGyiI8WFK0tQsdl4SDM7EzYF2hGg4LcD6fFcKGYQ-3K2_g-WRKbLFMF-bECQ5Ez7gMlYkFyaz4ECOq_n4sBvLirqCixTTfjTuGuc0R8jOyvH9kJJeNtQ_M9u86rXXbbKg5GFPcIPfatNzEtH0C-DAl-zNrOtUTwrBQmvuu_XvTUgZ-A3KVDJISPFJ4gZJ-b0Bo-J67JZLIcpkiy7xPnXarc7T5D3IXsFSlboxBiZslyHG58IFWaJ3PeLWXIVG3p91urItGow.eyJleHAiOjE0Mjc3Njk1NDYsImp0aSI6ImNhMGE0MzQzLTMzNzUtNDdkNC1hYWFlLTIxNGYxYTkwNTBkNCJ9.MfJgVAgGTB_ghVp1zNksj11kQzMoSCiTNOkSWrgXpFuYUySNHfXKqE7sZ-WWeWbVqghTcn73owRI4rbuFjpQRHOxGPLr_Ax-Rl0sWIwuOJUsgtoh1fBKvPF3dFzDcoREUxKQh3moO7vjEpFa1JRC8_I3BRw8ToenwWD95k7i9lbvz_2ar3Lt0--d8QzovVG0O2t5Onk32aUX2oydt2YBXf2HNG7AYBmzffLNBaTzaBD_SA6rguHTvClHFFjBficDMc-LUrOUgswG-Ml3XDX5qjlYR-6hm6WGptdaHBEpUeh7mr1j5XLBcf9ntg6nzTEjN10ZT-MMJGEcgpXJ9RzbFQ";
//		System.out.println("Token: " + token);
//		String uuid = manager.validateToken(token);
//		System.out.println("UUID: " + uuid);
	}
	
	public TokenValidator() {
		try {
			this.mongoClient = new MongoClient();
			this.publicKey = readKeyFromFile("public.key");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.db = mongoClient.getDB( "ttp_datastore" );
		Jongo jongo = new Jongo(this.db);
		docCollection = jongo.getCollection("documents");
	}
		
	public String validateToken(String token) throws InvalidJwtException, MalformedClaimException {
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
		.setRequireExpirationTime() // the JWT must have an expiration time
		.setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
		.setVerificationKey(this.publicKey) // verify the signature with the public key
		.build(); // create the JwtConsumer instance

		
		//  Validate the JWT and process it to the Claims
		JwtClaims jwtClaims;
		jwtClaims = jwtConsumer.processToClaims(token);
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
}
