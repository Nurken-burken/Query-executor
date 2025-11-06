package com.example.queryexecutor.config;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DatabaseConfig {

    private final DataSource dataSource;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            try {
                loadTitanicDataset();
                log.info("Titanic dataset loaded successfully");
            } catch (Exception e) {
                log.error("Failed to load Titanic dataset", e);
            }
        };
    }

    private void loadTitanicDataset() throws IOException, CsvException, SQLException {
        // Create table
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS passengers (
                        PassengerId INT PRIMARY KEY,
                        Survived INT,
                        Pclass INT,
                        Name VARCHAR(255),
                        Sex VARCHAR(10),
                        Age DECIMAL(5,2),
                        SibSp INT,
                        Parch INT,
                        Ticket VARCHAR(50),
                        Fare DECIMAL(10,4),
                        Cabin VARCHAR(50),
                        Embarked VARCHAR(10)
                    )
                    """;
            stmt.execute(createTableSQL);
            log.info("Passengers table created");
        }

        // Load CSV data
        ClassPathResource resource = new ClassPathResource("titanic.csv");
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            List<String[]> records = csvReader.readAll();
            
            // Skip header row
            records.remove(0);
            
            String insertSQL = """
                    INSERT INTO passengers 
                    (PassengerId, Survived, Pclass, Name, Sex, Age, SibSp, Parch, Ticket, Fare, Cabin, Embarked) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                
                for (String[] record : records) {
                    pstmt.setInt(1, parseInteger(record[0])); // PassengerId
                    pstmt.setInt(2, parseInteger(record[1])); // Survived
                    pstmt.setInt(3, parseInteger(record[2])); // Pclass
                    pstmt.setString(4, record[3]); // Name
                    pstmt.setString(5, record[4]); // Sex
                    
                    // Age can be null
                    if (record[5].isEmpty()) {
                        pstmt.setNull(6, java.sql.Types.DECIMAL);
                    } else {
                        pstmt.setBigDecimal(6, new java.math.BigDecimal(record[5]));
                    }
                    
                    pstmt.setInt(7, parseInteger(record[6])); // SibSp
                    pstmt.setInt(8, parseInteger(record[7])); // Parch
                    pstmt.setString(9, record[8]); // Ticket
                    
                    // Fare can be null
                    if (record[9].isEmpty()) {
                        pstmt.setNull(10, java.sql.Types.DECIMAL);
                    } else {
                        pstmt.setBigDecimal(10, new java.math.BigDecimal(record[9]));
                    }
                    
                    pstmt.setString(11, record.length > 10 ? record[10] : null); // Cabin
                    pstmt.setString(12, record.length > 11 ? record[11] : null); // Embarked
                    
                    pstmt.addBatch();
                }
                
                pstmt.executeBatch();
                log.info("Loaded {} passenger records", records.size());
            }
        }
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
