package com.realestate.util;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
  private Passwords() {}

  public static String hash(String password) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return "pbkdf2$210000$"
        + Base64.getEncoder().encodeToString(salt)
        + "$"
        + derive(password, salt, 210000);
  }

  private static String derive(String password, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
    try {
      return Base64.getEncoder()
          .encodeToString(
              SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                  .generateSecret(spec)
                  .getEncoded());
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException(ex);
    } finally {
      spec.clearPassword();
    }
  }

  public static boolean verify(String password, String stored) {
    try {
      String[] p = stored.split("\\$");
      return p.length == 4
          && p[0].equals("pbkdf2")
          && MessageDigest.isEqual(
              derive(password, Base64.getDecoder().decode(p[2]), Integer.parseInt(p[1]))
                  .getBytes(java.nio.charset.StandardCharsets.UTF_8),
              p[3].getBytes(java.nio.charset.StandardCharsets.UTF_8));
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
