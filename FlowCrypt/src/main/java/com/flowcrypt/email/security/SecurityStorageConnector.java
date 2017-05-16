package com.flowcrypt.email.security;

import android.content.Context;

import com.flowcrypt.email.security.model.PrivateKeyInfo;
import com.flowcrypt.email.test.PgpContact;
import com.flowcrypt.email.test.PgpKeyInfo;
import com.flowcrypt.email.test.StorageConnectorInterface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implemented StorageConnectorInterface. We collect an information about available
 * private keys.
 *
 * @author DenBond7
 *         Date: 05.05.2017
 *         Time: 13:06
 *         E-mail: DenBond7@gmail.com
 */

public class SecurityStorageConnector implements StorageConnectorInterface {

    private LinkedList<PgpKeyInfo> pgpKeyInfoList;
    private LinkedList<String> passphraseList;

    public SecurityStorageConnector(Context context) {
        this.pgpKeyInfoList = new LinkedList<>();
        this.passphraseList = new LinkedList<>();
        try {
            List<PrivateKeyInfo> privateKeysInfo = SecurityUtils.getPrivateKeysInfo(context);
            for (PrivateKeyInfo privateKeyInfo : privateKeysInfo) {
                pgpKeyInfoList.add(privateKeyInfo.getPgpKeyInfo());
                passphraseList.add(privateKeyInfo.getPassphrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public PgpContact findPgpContact(String longid) {
        return null;
    }

    @Override
    public PgpContact[] findPgpContacts(String[] longid) {
        return new PgpContact[0];
    }

    @Override
    public PgpKeyInfo getPgpPrivateKey(String longid) {
        for (PgpKeyInfo pgpKeyInfo : pgpKeyInfoList) {
            if (longid.equals(pgpKeyInfo.getLongid())) {
                return pgpKeyInfo;
            }
        }
        return null;
    }

    @Override
    public PgpKeyInfo[] getFilteredPgpPrivateKeys(String[] longId) {
        List<PgpKeyInfo> pgpKeyInfos = new ArrayList<>();
        for (String id : longId) {
            for (PgpKeyInfo pgpKeyInfo : this.pgpKeyInfoList) {
                if (pgpKeyInfo.getLongid().equals(id)) {
                    pgpKeyInfos.add(pgpKeyInfo);
                    break;
                }
            }
        }
        return pgpKeyInfos.toArray(new PgpKeyInfo[0]);
    }

    @Override
    public PgpKeyInfo[] getAllPgpPrivateKeys() {
        return pgpKeyInfoList.toArray(new PgpKeyInfo[0]);
    }

    @Override
    public String getPassphrase(String longid) {
        for (int i = 0; i < pgpKeyInfoList.size(); i++) {
            PgpKeyInfo pgpKeyInfo = pgpKeyInfoList.get(i);
            if (longid.equals(pgpKeyInfo.getLongid())) {
                return passphraseList.get(i);
            }
        }

        return null;
    }
}
