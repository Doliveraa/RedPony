package edu.csulb.phylo;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class KeyStore {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    /**
     * Constructer
     */
    public KeyStore(){
    }

    /**
     * Generates a private key pair
     * @param alias String of user;s alias; cannot be null
     * @return Return the key pair
     * @throws NoSuchAlgorithmException if there is no algorithm
     * @throws NoSuchProviderException if there is no provider
     * @throws InvalidAlgorithmParameterException if the parameters to the algorithm are invalid
     */
    public KeyPair keyGenerator(final String alias) throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException{

        final KeyPairGenerator kpg = KeyPairGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        kpg.initialize(new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        return kpg.generateKeyPair();
    }

    /**
     * Generates a secret key
     * @param alias String of the user's alias; cannot be null
     * @return the secret key
     * @throws NoSuchAlgorithmException if there is no algorithm
     * @throws NoSuchProviderException if there is no provider
     * @throws InvalidAlgorithmParameterException if the paramters to the algorithm are invalid
     */
    @NonNull
    private SecretKey getSecretKey(final String alias) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {

        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");

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
 //       return Collections.list(keyStore.aliases());
    }


    /**
     * Encrypts the key
     * @param alias The string of the user's name; cannot be null
     * @param keyGenerator the private key generated
     * @param keyGenParameterSpec the specs of the private key generated throw this exception
     * @throws NoSuchAlgorithmException if there is no algorithm throw this exception
     * @throws NoSuchPaddingException if there is no padding throw this exception
     * @throws InvalidAlgorithmParameterException if there is an invalid algorithm throw this exception
     * @throws InvalidKeyException if there is an invalid key throw this exception
     */
    private byte[] encryptData(String alias, KeyGenerator keyGenerator, KeyGenParameterSpec keyGenParameterSpec)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException{
        keyGenerator.init(keyGenParameterSpec);
        final SecretKey secretKey = keyGenerator.generateKey();

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

//        cipher.getIV();
//        return (cipher.doFinal(textToEncrypt.getBytes("UTF-8")));

    }

    /**
     * Decrpyts the data
     * @param alias string of the user's name; cannot be null
     */
    private void decryptData(String alias){

    }



}
