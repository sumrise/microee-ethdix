package com.microee.ethdix.j3.address;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.web3j.crypto.Credentials;

import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;

/**
 * 随机生成密钥和地址
 * @author keesh
 */
public class DeterministicKeys {

    // By default bitcoinj uses the path m/0'/0 for the chain of keys. 
    // And most of the Ethereum solutions uses m/44'/60'/0'/0 from the BIP44 specification. 
    // That's why the result wasn't as expected when comparing to other Ethereum tools.
    public static String[] generator(String seedCode, String passwd) throws UnreadableWalletException {
        // BitcoinJ
        DeterministicSeed seed = new DeterministicSeed(seedCode, null, passwd, Instant.now().toEpochMilli());
        DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(seed).build();
        List<ChildNumber> keyPath = HDUtils.parsePath("M/44H/60H/0H/0/0");
        DeterministicKey key = chain.getKeyByPath(keyPath, true);
        BigInteger privKey = key.getPrivKey();
        // Web3j
        Credentials credentials = Credentials.create(privKey.toString(16));
        return new String[] { credentials.getAddress(),  privKey.toString(16)};
    }
    
    // 随机生成助记词
    public static String generateNewMnemonic(int wordCount) {
        Words words = null;
        switch (wordCount) {
            case 24:
                words = Words.TWENTY_FOUR;
                break;
            case 18:
                words = Words.EIGHTEEN;
                break;
            case 15:
                words = Words.FIFTEEN;
                break;
            case 12:
                words = Words.TWELVE;
                break;
            case 21:
                words = Words.TWENTY_ONE;
                break;
            default:
                words = Words.TWELVE;
                break;
        }
        StringBuilder sb = new StringBuilder();
        byte[] entropy = new byte[words.byteLength()];
        new SecureRandom().nextBytes(entropy);
        new MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append);
        return sb.toString();
    }
    
}
