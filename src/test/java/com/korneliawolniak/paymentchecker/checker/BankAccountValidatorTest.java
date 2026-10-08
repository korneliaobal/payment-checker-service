package com.korneliawolniak.paymentchecker.checker;

import static org.junit.jupiter.api.Assertions.*;

import com.korneliawolniak.paymentprocessing.validation.BankAccountValidator;
import org.junit.jupiter.api.Test;

class BankAccountValidatorTest {
  @Test
  void acceptsIbanAndNationalFormatWithSpaces() {
    for (String value :
        new String[] {
          "PL61109010140000071219812874",
          "61109010140000071219812874",
          "pl61 1090 1014 0000 0712 1981 2874"
        }) {
      assertTrue(BankAccountValidator.isValid(value));
      assertEquals("PL61109010140000071219812874", BankAccountValidator.normalize(value));
    }
  }

  @Test
  void rejectsIncorrectChecksumAndMalformedAccounts() {
    for (String value :
        new String[] {
          "",
          "PL62109010140000071219812874",
          "PL6110901014000007121981287A",
          "DE61109010140000071219812874",
          "6110901014000007121981287"
        }) assertFalse(BankAccountValidator.isValid(value));
    assertFalse(BankAccountValidator.isValid(null));
  }
}
