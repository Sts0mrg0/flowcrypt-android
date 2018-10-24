/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.util.exception;

/**
 * @author Denis Bondarenko
 * Date: 24.10.2018
 * Time: 9:15
 * E-mail: DenBond7@gmail.com
 */
public class Issue347Exception extends FlowCryptException {
    public Issue347Exception() {
    }

    public Issue347Exception(String message) {
        super(message);
    }

    public Issue347Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Issue347Exception(Throwable cause) {
        super(cause);
    }
}
