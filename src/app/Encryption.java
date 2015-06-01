package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Encryption {

	private static Encryption instance;
	private File private_key = new File(".private");
	private File public_key = new File(".public");
	private String algorithm = "RSA";

	public static Encryption getInstance() {
		if (instance == null)
			instance = new Encryption();
		return instance;
	}

	private Encryption() {
		if(!areKeysPresent())
			generateKeys();
		}


	/********* areKeysPresent() **********/
	/**
	   @brief Checks if private and public keys are both presents
	   @return true if both are presents
	   @return false if they aren't both present
	 */
	public boolean areKeysPresent() {
		return private_key.exists() && public_key.exists();
	}
	
	/********** generateKeys() **********/
	/**
	   @brief Generates a couple of private and public keys
	 */
	public void generateKeys() {
		System.out.println("Keys not found");
	      try {
	    	  KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
	    	  keyGen.initialize(2048);
	    	  KeyPair key = keyGen.generateKeyPair();
	    	  private_key.createNewFile(); // Create files to store public and private key
	    	  public_key.createNewFile();
	    	  ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(public_key)); // Saving the Public key in a file
	    	  publicKeyOS.writeObject(key.getPublic());
	    	  publicKeyOS.close();
	    	  ObjectOutputStream privateKeyOS = new ObjectOutputStream(new FileOutputStream(private_key)); // Saving the Private key in a file
	    	  privateKeyOS.writeObject(key.getPrivate());
	    	  privateKeyOS.close();
	      } catch (NoSuchAlgorithmException | IOException e) {
	    	  System.err.println("Error while generating keys");
	    	  e.printStackTrace();
	      }
	      System.out.println("Keys generated");
	}

	/********** encrypt() **********/
	/**
	   @brief Encrypts a message
	   @param message is the message to be encrypted
	   @param publicKey is the publicKey we want to use
	   @return is the message encrypted using the key publicKey
	 */
	public byte[] encrypt(String message, String publicKey) {
		byte[] encrypted = null;
		try {
			@SuppressWarnings("resource")
			PublicKey key = (PublicKey) new ObjectInputStream(new FileInputStream(new File(publicKey))).readObject();
			final Cipher cipher = Cipher.getInstance(algorithm); // get an RSA cipher object and print the provider
			cipher.init(Cipher.ENCRYPT_MODE, key); // encrypt the plain text using the public key
			encrypted = cipher.doFinal(message.getBytes());
		} catch (BadPaddingException | ClassNotFoundException | IOException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
			System.err.println("Error while encrypting the message");
			e.printStackTrace();
		}
		return encrypted;
	}
	
	/********** encrypt() **********/
	/**
	   @brief Encrypts a message
	   @param message is the message to be encrypted
	   @param publicKey is the publicKey we want to use
	   @return is the message encrypted using the key publicKey
	 */
	/*public byte[] encrypt(byte[] message, String publicKey) {
		byte[] encrypted = null;
		try {
			@SuppressWarnings("resource")
			PublicKey key = (PublicKey) new ObjectInputStream(new FileInputStream(new File(publicKey))).readObject();
			final Cipher cipher = Cipher.getInstance(algorithm); // get an RSA cipher object and print the provider
			cipher.init(Cipher.ENCRYPT_MODE, key); // encrypt the plain text using the public key
			encrypted = cipher.doFinal(message);
		} catch (BadPaddingException | ClassNotFoundException | IOException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
			System.err.println("Error while encrypting the message");
			e.printStackTrace();
		}
		return encrypted;
	}*/

	/********** decrypt() **********/
	/**
	   @brief Decrypts a message
	   @param bytes is the message to be decrypted
	   @return is the message decrypted using my private key
	 */
	public String decrypt(byte[] bytes) {
		byte[] dectyptedText = null;
		try {
			@SuppressWarnings("resource")
			PrivateKey key = (PrivateKey) new ObjectInputStream(new FileInputStream(private_key)).readObject();
			Cipher cipher = Cipher.getInstance(algorithm); // get an RSA cipher object and print the provider
			cipher.init(Cipher.DECRYPT_MODE, key); // decrypt the text using the private key
			dectyptedText = cipher.doFinal(bytes);
		} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			//System.err.println("Error while decrypting the message");
			//e.printStackTrace();
		}
		return new String(dectyptedText, 0, dectyptedText.length);
	}

	/********** decryptAttach() **********/
	/**
	   @brief Decrypts a message
	   @param bytes is the message to be decrypted
	   @return is the message decrypted using my private key
	 */
	/*public byte[] decryptAttach(byte[] bytes) {
		byte[] dectyptedText = null;
		try {
			@SuppressWarnings("resource")
			PrivateKey key = (PrivateKey) new ObjectInputStream(new FileInputStream(private_key)).readObject();
			Cipher cipher = Cipher.getInstance(algorithm); // get an RSA cipher object and print the provider
			cipher.init(Cipher.DECRYPT_MODE, key); // decrypt the text using the private key
			dectyptedText = cipher.doFinal(bytes);
		} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Error while decrypting the message");
			e.printStackTrace();
		}
		return dectyptedText;
	}*/
}
