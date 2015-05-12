package edu.uccs.cs5920.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;



public class JOSEEncryptionTest {
	/**
	 * Based on examples from
	 * http://www.javamex.com/tutorials/cryptography/rsa_encryption.shtml
	 */

	public static void main(String[] args) throws Exception {
		JOSEEncryptionTest manager = new JOSEEncryptionTest();
		
		JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.RSA1_5);
		jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_GCM);
		RSAPublicKey publicKey = (RSAPublicKey) manager.readKeyFromFile("public.key");
		jwe.setKey(publicKey);
		
		byte data[] = UUID.randomUUID().toString().getBytes();
		System.out.println("Plaintext: " + new String(data));
		
		//1.  Encrypte plaintext
		jwe.setPayload(new String(data));
		String serializedJwe = jwe.getCompactSerialization();
		System.out.println("Serialized Encrypted JWE: " + serializedJwe);
		

		RSAPrivateKey privateKey = (RSAPrivateKey) manager.readPrivKeyFromFile("private.key");
		JsonWebEncryption jwe2 = new JsonWebEncryption();
		jwe2.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.RSA1_5);
		jwe2.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_GCM);
		
		jwe2.setKey(privateKey);
		jwe2.setCompactSerialization(serializedJwe);
		System.out.println("Payload: " + jwe2.getPayload());
//		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
//        //.setRequireExpirationTime() // the JWT must have an expiration time
//        //.setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
//        //.setRequireSubject() // the JWT must have a subject claim
//        //.setExpectedIssuer("Issuer") // whom the JWT needs to have been issued by
//        //.setExpectedAudience("Audience") // to whom the JWT is intended for
//        .setVerificationKey(publicKey) // verify the signature with the public key
//        .build(); 
//		
//		try
//	    {
//	        //  Validate the JWT and process it to the Claims
//	        JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
//	        System.out.println("JWT validation succeeded! " + jwtClaims);
//	    }
//	    catch (InvalidJwtException e)
//	    {
//	        // InvalidJwtException will be thrown, if the JWT failed processing or validation in anyway.
//	        // Hopefully with meaningful explanations(s) about what went wrong.
//	        System.out.println("Invalid JWT! " + e);
//	    }

	}

	public PublicKey readKeyFromFile(String keyFileName) throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(keyFileName);
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(keySpec);
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

	public byte[] rsaEncrypt(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		PublicKey pubKey = readKeyFromFile("public.key");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}

	public byte[] rsaDecrypt(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		PrivateKey privKey = readPrivKeyFromFile("private.key");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
	}
	
	public void saveToFile(String fileName, BigInteger mod, BigInteger exp) throws IOException {
		  ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		  try {
		    oout.writeObject(mod);
		    oout.writeObject(exp);
		  } catch (Exception e) {
		    throw new IOException("Unexpected error", e);
		  } finally {
		    oout.close();
		  }
		}
}
