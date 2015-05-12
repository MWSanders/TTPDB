package edu.uccs.cs5920.broker;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class KeyGenerator {

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		KeyGenerator generator = new KeyGenerator();
		generator.generateKeys();
	}
	
	public void generateKeys() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.genKeyPair();
		Key publicKey = (Key) kp.getPublic();
		Key privateKey = (Key) kp.getPrivate();
		
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
		RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

		saveToFile("src/main/resources/public.key", pub.getModulus(), pub.getPublicExponent());
		saveToFile("src/main/resources/private.key", priv.getModulus(), priv.getPrivateExponent());
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
