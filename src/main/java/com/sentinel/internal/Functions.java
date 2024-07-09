package com.sentinel.internal;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Yogesh malviya
 */
public class Functions {
    private static String ALL_CHARS;
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS_A = "*$!@#&";
    private static final String SPECIAL_CHARS_B = "%?+^/-";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom random = new SecureRandom();
    
    public static String randomID(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i=0; i<length; i++)
            builder.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        return builder.toString();
    }
    
    public static String generatePassword(int length, boolean upper, boolean lower, boolean digits, boolean specialA, boolean specialB) {
        StringBuilder password = new StringBuilder(length);
        ALL_CHARS = null;
        int active = 0;
        if (upper) {
            password.append(UPPER.charAt(random.nextInt(UPPER.length())));
            ALL_CHARS += UPPER;
            active += 1;
        }
        if (lower) {
            password.append(LOWER.charAt(random.nextInt(LOWER.length())));
            ALL_CHARS += LOWER;
            active += 1;
        }
        if (digits) {
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
            ALL_CHARS += DIGITS;
            active += 1;
        }
        if (specialA) {
            password.append(SPECIAL_CHARS_A.charAt(random.nextInt(SPECIAL_CHARS_A.length())));
            ALL_CHARS += SPECIAL_CHARS_A;
            active += 1;
        }
        if (specialB) {
            password.append(SPECIAL_CHARS_B.charAt(random.nextInt(SPECIAL_CHARS_B.length())));
            ALL_CHARS += SPECIAL_CHARS_B;
            active += 1;
        }
        for (int i=active; i<length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        char[] charArray = password.toString().toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            int randomIndex = random.nextInt(charArray.length);
            char temp = charArray[i];
            charArray[i] = charArray[randomIndex];
            charArray[randomIndex] = temp;
        }
        return new String(charArray);
    }
    
    public static String getSalt() {
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        return toHex(salt);
    }

    private static String toHex(byte[] array) {
        java.math.BigInteger bi = new java.math.BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }
    
    public static void writeKeyFile(String keyData, String keyFile) {
        java.io.File vltKey = new java.io.File(keyFile);
        if (vltKey.exists())
            vltKey.delete();
        try (java.io.FileWriter keyWriter = new java.io.FileWriter(vltKey)) {
            keyWriter.write(keyData);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static String readKeyFile(String keyFile) {
        String keyData = null;
        try (java.io.BufferedReader keyReader = new java.io.BufferedReader(new java.io.FileReader(keyFile))) {
            keyData = keyReader.readLine();
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return keyData;
    }
    
    public static TableModel vltTableModel(String vltTableName) {
        ResultSet vltResult = SQLite.vltTableData(vltTableName);
        DefaultTableModel vltTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        };
        try {
            ResultSetMetaData vltMeta = vltResult.getMetaData();
            vltTableModel.addColumn(vltMeta.getColumnLabel(1));
            vltTableModel.addColumn(vltMeta.getColumnLabel(2));
            vltTableModel.addColumn(vltMeta.getColumnLabel(3));
            vltTableModel.addColumn(vltMeta.getColumnLabel(4));
            vltTableModel.addColumn(vltMeta.getColumnLabel(5));
            vltTableModel.addColumn(vltMeta.getColumnLabel(6));
            Object[] row = new Object[6];
            while (vltResult.next()) {
                row[0] = vltResult.getObject(1);
                row[1] = vltResult.getObject(2);
                row[2] = vltResult.getObject(3);
                row[3] = vltResult.getObject(4);
                row[4] = vltResult.getObject(5);
                row[5] = vltResult.getObject(6);
                vltTableModel.addRow(row);
            }
            return vltTableModel;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
