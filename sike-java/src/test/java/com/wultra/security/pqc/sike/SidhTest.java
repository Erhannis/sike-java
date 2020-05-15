/*
 * Copyright 2020 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wultra.security.pqc.sike;

import com.wultra.security.pqc.sike.crypto.KeyGenerator;
import com.wultra.security.pqc.sike.crypto.Sidh;
import com.wultra.security.pqc.sike.math.Fp2Element;
import com.wultra.security.pqc.sike.model.ImplementationType;
import com.wultra.security.pqc.sike.model.Party;
import com.wultra.security.pqc.sike.param.SikeParam;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.Security;

/**
 * Test of SIDH key exchange.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class SidhTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testSidh() {
        SikeParam sikeParam = new SikeParam("SIKEp434", ImplementationType.REFERENCE);
        System.out.println("Prime: " + sikeParam.getPrime());
        KeyGenerator keyGenerator = new KeyGenerator(sikeParam);
        Sidh sidh = new Sidh(sikeParam);
        int loopCount = 3;
        long timeTotal = 0L;
        for (int i = 0; i < loopCount; i++) {
            System.out.println("----------------------------------------");
            long timeStart = System.currentTimeMillis();
            KeyPair keyPairA = keyGenerator.generateKeyPair(Party.ALICE);
            System.out.println("Alice's keypair:");
            System.out.println("Private key: " + keyPairA.getPrivate());
            System.out.println("Public key: " + keyPairA.getPublic());
            KeyPair keyPairB = keyGenerator.generateKeyPair(Party.BOB);
            System.out.println("Bob's keypair:");
            System.out.println("Private key: " + keyPairB.getPrivate());
            System.out.println("Public key: " + keyPairB.getPublic());
            // Bob's public key is sent to Alice
            Fp2Element secretA = sidh.generateSharedSecret(Party.ALICE, keyPairA.getPrivate(), keyPairB.getPublic());
            System.out.println("Shared secret generated by Alice: " + secretA);
            // Alice's public key is sent to Bob
            Fp2Element secretB = sidh.generateSharedSecret(Party.BOB, keyPairB.getPrivate(), keyPairA.getPublic());
            System.out.println("Shared secret generated by Bob: " + secretB);
            boolean match = secretA.equals(secretB);
            System.out.println("Secrets match: " + match);
            if (!match) {
                throw new RuntimeException("Secrets do not match");
            }
            long timeEnd = System.currentTimeMillis();
            timeTotal += (timeEnd - timeStart);
        }
        System.out.println("Average execution time: " + ((double)timeTotal / loopCount) + " ms");
    }
}
