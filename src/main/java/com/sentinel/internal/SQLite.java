package com.sentinel.internal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Hemant Kumar Morya
 */
public class SQLite {
    static Connection vltDB = null;
    static Connection listDB = null;
    static File dataFolder = new File("data");
    static Path currentDirectory = Paths.get(System.getProperty("user.dir"));
    
    public static void initVaultList() {
        if (!Files.isWritable(currentDirectory))
            dataFolder = new File(System.getenv("appdata") + File.separator + "Sentinel");
        dataFolder.mkdir();
        try {
            listDB = DriverManager.getConnection("jdbc:sqlite:" + dataFolder + File.separator + "vaults.lst");
            String vltsTable = "CREATE TABLE IF NOT EXISTS Vaults (Path TEXT NOT NULL UNIQUE, Name TEXT, HasKey BOOLEAN, KeyPath TEXT, LastOpened TEXT, PRIMARY KEY(Path));";
            Statement listDBquery = listDB.createStatement();
            listDBquery.execute(vltsTable);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static DefaultListModel<String> getVaultList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        String vltsTable = "SELECT Path FROM Vaults ORDER BY LastOpened DESC;";
        try {
            Statement listDBquery = listDB.createStatement();
            ResultSet listResult = listDBquery.executeQuery(vltsTable);
            while (listResult.next()) {
                listModel.addElement(listResult.getString("Path"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return listModel;
    }
    
    public static String getVaultName(String vltPath) {
        String vltsTable = "SELECT Name FROM Vaults WHERE Path = ?;";
        String vltName = null;
        try {
            PreparedStatement listDBquery = listDB.prepareStatement(vltsTable);
            listDBquery.setString(1, vltPath);
            ResultSet vltResult = listDBquery.executeQuery();
            vltName = vltResult.getString("Name");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return vltName;
    }
    
    public static boolean getVaultHasKey(String vltPath) {
        String vltsTable = "SELECT HasKey FROM Vaults WHERE Path = ?;";
        boolean vltHasKey = false;
        try {
            PreparedStatement listDBquery = listDB.prepareStatement(vltsTable);
            listDBquery.setString(1, vltPath);
            ResultSet vltResult = listDBquery.executeQuery();
            vltHasKey = vltResult.getBoolean("HasKey");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return vltHasKey;
    }
    
    public static String getVaultKeyPath(String vltPath) {
        String vltsTable = "SELECT KeyPath FROM Vaults WHERE Path = ?;";
        String vltKeyPath = null;
        try {
            PreparedStatement listDBquery = listDB.prepareStatement(vltsTable);
            listDBquery.setString(1, vltPath);
            ResultSet vltResult = listDBquery.executeQuery();
            vltKeyPath = vltResult.getString("KeyPath");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return vltKeyPath;
    }
    
    public static void setVaultList(String vltPath, String vltName, boolean vltKey, String valtKey) {
        String vltsTable = "REPLACE INTO Vaults (Path, Name, HasKey, KeyPath, LastOpened) VALUES(?, ?, ?, ?, datetime('now'));";
        try {
            PreparedStatement listDBquery = listDB.prepareStatement(vltsTable);
            listDBquery.setString(1, vltPath);
            listDBquery.setString(2, vltName);
            listDBquery.setBoolean(3, vltKey);
            listDBquery.setString(4, valtKey);
            listDBquery.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void remVaultList(){
        String vltsTable = "DELETE FROM Vaults;";
        try {
            Statement listDBquery = listDB.createStatement();
            listDBquery.execute(vltsTable);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void closeVaultList() {
        if (listDB != null) {
            try {
                if (listDB.isValid(5)) {
                    listDB.close();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static boolean createNewVault(String vltName, String vltPass, String vltPath) {
        String rootTable = "CREATE TABLE IF NOT EXISTS Root (\"UID\" TEXT NOT NULL UNIQUE, \"Title\" TEXT, \"Username\" TEXT, \"Password\" TEXT, \"URL\" TEXT, \"Notes\" TEXT, PRIMARY KEY(\"UID\"));";
        String allTables = "CREATE TABLE IF NOT EXISTS AllTables (\"Table Name\" TEXT NOT NULL UNIQUE, PRIMARY KEY(\"Table Name\"));";
        String confTable = "CREATE TABLE IF NOT EXISTS Configuration (Parameter TEXT NOT NULL UNIQUE, Value TEXT, PRIMARY KEY(Parameter));";
        String confValue = "INSERT INTO Configuration (Parameter, Value) VALUES('Vault Name', ?);";
        try {
            File vltFile = new File(vltPath);
            if (vltFile.exists())
                vltFile.delete();
            vltDB = DriverManager.getConnection("jdbc:sqlite:" + vltFile, org.sqlite.mc.SQLiteMCChacha20Config.getDefault().withKey(vltPass).build().toProperties());
            Statement vltDBquery = vltDB.createStatement();
            vltDBquery.execute(rootTable);
            vltDBquery.execute(confTable);
            vltDBquery.execute(allTables);
            PreparedStatement vltDBquery2 = vltDB.prepareStatement(confValue);
            vltDBquery2.setString(1, vltName);
            vltDBquery2.executeUpdate();
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Details: " + ex, "Error Creating Vault File", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public static boolean openVault(String vltPath, String vltPass) {
        try {
            vltDB = DriverManager.getConnection("jdbc:sqlite:" + new File(vltPath), org.sqlite.mc.SQLiteMCChacha20Config.getDefault().withKey(vltPass).build().toProperties());
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, String.format("An error occurred while trying to read the Vault File. Invalid credentials were provided, please try again."
                + "\n\nDetails: %s\n\nIf this reoccurs, then your Vault File may be corrupt.", ex), "Error Reading Vault File", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public static String getVaultName() {
        String allTables = "SELECT Value FROM Configuration WHERE Parameter = 'Vault Name';";
        String vltName = null;
        try (Statement vltDBquery = vltDB.createStatement()) {
            ResultSet vltResult = vltDBquery.executeQuery(allTables);
            vltName = vltResult.getString("Value");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return vltName;
    }
    
    public static void setVaultName(String vltName) {
        String confValue = "REPLACE INTO Configuration (Parameter, Value) VALUES('Vault Name', ?);";
        try {
            PreparedStatement vltDBquery = vltDB.prepareStatement(confValue);
            vltDBquery.setString(1, vltName);
            vltDBquery.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void vltPassword(String vltPass) {
        String vltNewPass = String.format("PRAGMA rekey='%s'", vltPass);
        try {
            Statement vltDBquery = vltDB.createStatement();
            vltDBquery.execute(vltNewPass);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static DefaultTreeModel allTablesList() {
        DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode treeNode2;
        String allTables = "SELECT \"Table Name\" FROM AllTables;";
        try {
            Statement vltDBquery = vltDB.createStatement();
            ResultSet vltResult = vltDBquery.executeQuery(allTables);
            while (vltResult.next()) {
                treeNode2 = new DefaultMutableTreeNode(vltResult.getString("Table Name"));
                treeNode1.add(treeNode2);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return new DefaultTreeModel(treeNode1);
    }
    
    public static boolean vltTableExists(String vltTableName) {
        String allTables = "SELECT \"Table Name\" FROM AllTables;";
        boolean Exists = false;
        try {
            Statement vltDBquery = vltDB.createStatement();
            ResultSet vltResult = vltDBquery.executeQuery(allTables);
            while (vltResult.next()) {
                if (vltResult.getString("Table Name") == null ? vltTableName == null : vltResult.getString("Table Name").equals(vltTableName))
                    Exists = true;
                if (vltTableName == null)
                    Exists = false;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return Exists;
    }
    
    public static void newTable(String vltTableName){
        String allTables = "REPLACE INTO AllTables (\"Table Name\") VALUES(?);";
        String vltTable = String.format("CREATE TABLE IF NOT EXISTS \"%s\" (\"UID\" TEXT NOT NULL UNIQUE, \"Title\" TEXT, \"Username\" TEXT, \"Password\" TEXT, \"URL\" TEXT, \"Notes\" TEXT, PRIMARY KEY(\"UID\"));", vltTableName);
        try {
            PreparedStatement vltDBquery = vltDB.prepareStatement(allTables);
            vltDBquery.setString(1, vltTableName);
            vltDBquery.executeUpdate();
            Statement vltDBquery2 = vltDB.createStatement();
            vltDBquery2.execute(vltTable);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void renTable(String vltTableNameNew, String vltTableNameOld){
        String allTables = "UPDATE AllTables SET \"Table Name\" = ? WHERE \"Table Name\" = ?;";
        String vltTable = String.format("ALTER TABLE \"%s\" RENAME TO \"%s\";", vltTableNameOld, vltTableNameNew);
        try {
            PreparedStatement vltDBquery = vltDB.prepareStatement(allTables);
            vltDBquery.setString(1, vltTableNameNew);
            vltDBquery.setString(2, vltTableNameOld);
            vltDBquery.executeUpdate();
            Statement vltDBquery2 = vltDB.createStatement();
            vltDBquery2.execute(vltTable);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void remTable(String vltTableName){
        String allTables = "DELETE FROM AllTables WHERE \"Table Name\" = ?;";
        String vltTable = String.format("DROP TABLE \"%s\";", vltTableName);
        try {
            PreparedStatement vltDBquery = vltDB.prepareStatement(allTables);
            vltDBquery.setString(1, vltTableName);
            vltDBquery.executeUpdate();
            Statement vltDBquery2 = vltDB.createStatement();
            vltDBquery2.execute(vltTable);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static ResultSet vltTableData(String vltTableName) {
        ResultSet vltResult = null;
        String vltTable = String.format("SELECT * FROM \"%s\";", vltTableName);
        try {
            Statement vltDBquery = vltDB.createStatement();
            vltResult = vltDBquery.executeQuery(vltTable);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return vltResult;
    }
    
    public static void newTableEntry(String vltTableName, String UID, String Title, String Username, String Password, String URL, String Notes) {
        String vltTable = String.format("REPLACE INTO \"%s\" (\"UID\", \"Title\", \"Username\", \"Password\", \"URL\", \"Notes\") VALUES(?, ?, ?, ?, ?, ?);", vltTableName);
        try {
            PreparedStatement vltDBquery = vltDB.prepareStatement(vltTable);
            vltDBquery.setString(1, UID);
            vltDBquery.setString(2, Title);
            vltDBquery.setString(3, Username);
            vltDBquery.setString(4, Password);
            vltDBquery.setString(5, URL);
            vltDBquery.setString(6, Notes);
            vltDBquery.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void remTableEntry(String vltTableName, String UID) {
        String vltTable = String.format("DELETE FROM \"%s\" WHERE \"UID\" = ?;", vltTableName);
        try {
            PreparedStatement vltDBquery = vltDB.prepareStatement(vltTable);
            vltDBquery.setString(1, UID);
            vltDBquery.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
        
    public static void closeVault() {
        if (vltDB != null) {
            try {
                if (vltDB.isValid(5)) {
                    vltDB.close();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
