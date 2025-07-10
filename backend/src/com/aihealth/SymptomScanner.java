package com.aihealth;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

public class SymptomScanner {
    private static final Map<String, String> symptomsToDisease = new HashMap<>();
    private static final Map<String, Map<String, String>> treatmentByAge = new HashMap<>();

    static {
        symptomsToDisease.put("fever", "Common Cold");
        symptomsToDisease.put("fever, cough", "Common Cold");

        symptomsToDisease.put("cough", "Flu");
        symptomsToDisease.put("headache", "Migraine");
        symptomsToDisease.put("rash", "Allergy");

        Map<String, String> coldTreatment = new HashMap<>();
        coldTreatment.put("child", "Give Paracetamol syrup, keep hydrated");
        coldTreatment.put("adult", "Take Paracetamol tablets, rest and fluids");

        Map<String, String> fluTreatment = new HashMap<>();
        fluTreatment.put("child", "Use nasal spray, consult pediatrician");
        fluTreatment.put("adult", "Antiviral tablets, rest for 5 days");

        Map<String, String> migraineTreatment = new HashMap<>();
        migraineTreatment.put("child", "Consult doctor, avoid bright light");
        migraineTreatment.put("adult", "Take painkillers, reduce screen time");

        Map<String, String> allergyTreatment = new HashMap<>();
        allergyTreatment.put("child", "Antihistamine syrup");
        allergyTreatment.put("adult", "Antihistamine tablet or nasal spray");

        treatmentByAge.put("Common Cold", coldTreatment);
        treatmentByAge.put("Flu", fluTreatment);
        treatmentByAge.put("Migraine", migraineTreatment);
        treatmentByAge.put("Allergy", allergyTreatment);
    }

    public static String diagnose(String input, int age) {
        String[] userSymptoms = input.toLowerCase().split(",");
        String ageGroup = (age >= 1 && age <= 10) ? "child" : "adult";
        Set<String> diagnosed = new LinkedHashSet<>();
        StringBuilder result = new StringBuilder();

        for (String userSymptom : userSymptoms) {
            userSymptom = userSymptom.trim().replaceAll("[^a-z]", "");
            for (String knownSymptom : symptomsToDisease.keySet()) {
                if (getSimilarity(userSymptom, knownSymptom) >= 0.6) {
                    String disease = symptomsToDisease.get(knownSymptom);
                    if (!diagnosed.contains(disease)) {
                        diagnosed.add(disease);
                        result.append("🩺 Diagnosis: ").append(disease).append("\n");
                        result.append("💊 Treatment for ").append(ageGroup).append(": ")
                                .append(treatmentByAge.get(disease).get(ageGroup)).append("\n\n");
                    }
                }
            }
        }

        String finalResult = result.toString().isEmpty() ? "No known diseases detected." : result.toString().trim();
        storeDiagnosis(input, age, finalResult);
        return finalResult;
    }

    private static void storeDiagnosis(String symptoms, int age, String diagnosis) {
        MongoDatabase db = MongoConnector.connect();
        MongoCollection<Document> collection = db.getCollection("diagnosis data is saved");

        Document doc = new Document("symptoms", symptoms)
                .append("age", age)
                .append("diagnosis", diagnosis)
                .append("timestamp", new Date());

        collection.insertOne(doc);
        System.out.println("📥 Diagnosis saved to MongoDB.");
    }

    private static double getSimilarity(String s1, String s2) {
        int distance = levenshtein(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        return (maxLen == 0) ? 1.0 : 1.0 - (double) distance / maxLen;
    }

    private static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
}
}