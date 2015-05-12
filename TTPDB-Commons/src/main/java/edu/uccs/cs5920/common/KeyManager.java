package edu.uccs.cs5920.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;



public class KeyManager {
	/**
	 * Based on examples from
	 * http://www.javamex.com/tutorials/cryptography/rsa_encryption.shtml
	 */

	public static void main(String[] args) throws Exception {
		KeyManager manager = new KeyManager();
		java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
		java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
		
		byte data[] = UUID.randomUUID().toString().getBytes();
		System.out.println("Plaintext: " + new String(data));
		
		//1.  Encrypte plaintext
		byte encData[] = manager.rsaEncrypt(data);
		//System.out.println("Encrypted value: " + new String(encData));

//		Signature sig = Signature.getInstance("SHA1withRSA");
//		sig.initSign(manager.readPrivKeyFromFile("private.key"));
//		sig.update(data);
//		byte[] sigBytes = sig.sign();
//		System.out.println("sigBytes: " + new String(encoder.encode(sigBytes)));
		
		//2. encode into base 64
		String enc64Data = "gdeJ6yfSEJFQs9gUWM97QMeRVwLN8Sk4dTni7+SDVhrOYki//ir4/c1dNhGwP+4zQxnfOStzHSCYszulwebdTCiQ+q5gT9CPluNtgUFho5x2V/PUA3tal4iV0X7+WSvPcw6+1S4sSGzMHZPsDVE43xwbCL+hlK62T5QsHwBd/vLl5U8tGjvKXZqBFG+c02WkFTnAsOKoQu/BqXZtW3/8/CsyJV36tUaABfgLc2li2pfby8e5jON+aLlc+wQi/Hr3+Om/ubJAnhYx38/UM786tAubebUXF0BZirWaugxBlGNzIr7PlYR229nCuAILuiCYY/PN6e345f2iwP+d/jwJSQ==";
		//String enc64Data = encoder.encodeToString(encData);
		System.out.println("Encrypted Encoded value:" + enc64Data);
		
		//3. decode base64->bin
		byte encBinData[] = decoder.decode(enc64Data);
		//System.out.println("Encrypted Decoded value: " + new String(encBinData));
		
		//4. decrypt bin
		byte decData[] = manager.rsaDecrypt(encBinData);
		//System.out.println("Decrypted Decoded value: " + new String(decData));
		
		
//		sig.initVerify(manager.readKeyFromFile("public.key"));
//		sig.update(data);
//		System.out.println("verify: " +sig.verify(sigBytes));
		System.out.println("Plaintext: " + new String(decData));

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
}
