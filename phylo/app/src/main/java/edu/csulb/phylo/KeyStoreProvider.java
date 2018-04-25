package edu.csulb.phylo;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * To use, generate a secret key. Use that key to encrypt a string using encryptData.
 * To decrypt it, use decryptData
 */
public class KeyStoreProvider {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private KeyStore keystore;
    byte [] iv;

    /**
     * Constructer
     */
    public KeyStoreProvider(){
    }

//    /**
//     * Generates a private key pair
//     * @param alias String of user;s alias; cannot be null
//     * @return Return the key pair
//     * @throws NoSuchAlgorithmException if there is no algorithm
//     * @throws NoSuchProviderException if there is no provider
//     * @throws InvalidAlgorithmParameterException if the parameters to the algorithm are invalid
//     */
//    public KeyPair keyGenerator(final String alias) throws NoSuchAlgorithmException, NoSuchProviderException,
//            InvalidAlgorithmParameterException{
//
//        //Private Key Pair
//        final KeyPairGenerator kpg = KeyPairGenerator
//                .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
//
//        //Set the key properties
//        kpg.initialize(new KeyGenParameterSpec.Builder(alias,
//                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//                .build());
//
//        return kpg.generateKeyPair();
//    }

    /**
     * Generates a secret key
     * @param alias String of the user's alias; cannot be null
     * @return the secret key
     * @throws NoSuchAlgorithmException if there is no algorithm
     * @throws NoSuchProviderException if there is no provider
     * @throws InvalidAlgorithmParameterException if the paramters to the algorithm are invalid
     */
    @NonNull
    private SecretKey genSecretKey(final String alias) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {

        //Secret Key
        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");

        //Set the key properties
        keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        return keyGenerator.generateKey();
    }

    /**
     * List all the current entries in Android Key Store
     * @return the entries in the Android Key Store
     */
    private ArrayList<String> getAllAliasesInTheKeystore() throws KeyStoreException {
        return Collections.list(keystore.aliases());
    }

    /**
     * Encrypts the key
     * @return return the encrypted texT
     * @throws NoSuchAlgorithmException if there is no algorithm throw this exception
     * @throws NoSuchPaddingException if there is no padding throw this exception
     * @throws InvalidKeyException if there is an invalid key throw this exception
     */
    private byte[] encryptData(SecretKey secretKey, String textToEncrypt)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, UnsupportedEncodingException,
            IllegalBlockSizeException, BadPaddingException{

        //Start encrypting given our secret key
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        iv = cipher.getIV(); //we need this value for decryption
        return (cipher.doFinal(textToEncrypt.getBytes("UTF-8")));

    }

    /**
     * Decrypts the data
     * @param alias String of the user's name; cannot be null
     * @param encryptedData the data to decrypt
     * @return the decrypted string
     * @throws KeyStoreException if there is no key store
     * @throws NoSuchAlgorithmException if there is no algorithm
     * @throws IOException if there is an IO exception
     * @throws CertificateException if there is a certificate exception
     * @throws UnrecoverableEntryException if there an unrecoverable entry
     * @throws InvalidKeyException if there is a invalid key
     * @throws InvalidAlgorithmParameterException if there are invalid parameters to the aglorithm
     * @throws BadPaddingException if there is bad padding
     * @throws IllegalBlockSizeException if there is an illegal block size
     * @throws NoSuchPaddingException if there is no such padding
     */
    private String decryptData(String alias, byte [] encryptedData) throws KeyStoreException, NoSuchAlgorithmException,
            IOException, CertificateException, UnrecoverableEntryException, InvalidKeyException,
            InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException{
        //Getting an instance from the keystore
        keystore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keystore.load(null);

        //Get the secret key from the keystore that is related to our alias
        final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keystore
                .getEntry(alias, null);

        final SecretKey secretKey = secretKeyEntry.getSecretKey();

        //Start decrypting using that secret key
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        final byte[] decodedData = cipher.doFinal(encryptedData);
        final String unencryptedString = new String(decodedData, "UTF-8");
        return unencryptedString;
    }

//    private void encryptAstralKey(){
//        final String alias = "User";
//        String astralKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6IjVhYzA0ODkzMGM3NjRmMDMxMDYxYTY5YSJ9.HcgYhZ2n2zQjAPLHJpcE5HKLiHIdLcksNJVOTOudO4Y";
//        SecretKey secretKey = genSecretKey(alias);
//    }

}
