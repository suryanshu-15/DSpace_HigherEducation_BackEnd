package org.dspace.app.rest.hed;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4000")
@RestController
@RequestMapping("/api/hed")
public class HedInstitutionController {

    @Autowired
    private JdbcTemplate jdbc;

    @GetMapping("/branches")
    public ResponseEntity<List<Map<String, Object>>> getBranches() {
        String sql = """
            SELECT code, label, display_order
            FROM hed_branch
            WHERE active = true
            ORDER BY display_order
            """;
        return ResponseEntity.ok(jdbc.queryForList(sql));
    }

    // ── GET /api/hed/subtypes?branch=college ───────────────────────────────

    @GetMapping("/subtypes")
    public ResponseEntity<List<Map<String, Object>>> getSubTypes(
            @RequestParam(name = "branch", required = false) String branch,
            @RequestParam(name = "parent", required = false) String parent) {

        StringBuilder sql = new StringBuilder("""
            SELECT code, label, group_name, has_children, display_order
            FROM hed_section_type
            WHERE active = true
            """);

        java.util.List<Object> params = new java.util.ArrayList<>();

        if (parent != null && !parent.isBlank()) {
            sql.append(" AND parent_code = ? ");
            params.add(parent);
        } else if (branch != null && !branch.isBlank()) {
            sql.append(" AND branch_code = ? AND parent_code IS NULL ");
            params.add(branch);
        }

        sql.append(" ORDER BY display_order");

        return ResponseEntity.ok(jdbc.queryForList(sql.toString(), params.toArray()));
    }

    // ── GET /api/hed/districts ─────────────────────────────────────────────

    @GetMapping("/districts")
    public ResponseEntity<List<Map<String, Object>>> getDistricts() {
        String sql = """
            SELECT code, name, display_order
            FROM hed_district
            ORDER BY display_order
            """;
        return ResponseEntity.ok(jdbc.queryForList(sql));
    }

    // ── GET /api/hed/institutions ──────────────────────────────────────────
    //   Query params:
    //     sub_type  (required) e.g. GC, NGC_488, SPU
    //     district  (optional) e.g. KHD
    //     q         (optional) search string
    //     limit     (optional, default 30)

    @GetMapping("/institutions")
    public ResponseEntity<List<Map<String, Object>>> getInstitutions(
            @RequestParam(name = "sub_type")              String subType,
            @RequestParam(name = "district",  required = false) String district,
            @RequestParam(name = "q",         required = false) String query,
            @RequestParam(name = "limit",     defaultValue = "30") int limit) {

        // Strip RDE prefix to map back to default institution sub_type_codes
        String finalSubType = subType;
        if (subType.matches("RDE_[A-Z]{3}_NGC_488")) {
            finalSubType = "NGC_488";
        } else if (subType.matches("RDE_[A-Z]{3}_NGC_662")) {
            finalSubType = "NGC_662";
        } else if (subType.matches("RDE_[A-Z]{3}_GC")) {
            finalSubType = "GC";
        } else if (subType.matches("RDE_[A-Z]{3}_SC")) {
            finalSubType = "SC";
        } else if (subType.matches("RDE_[A-Z]{3}_PVT")) {
            finalSubType = "PVT";
        }

        StringBuilder sql = new StringBuilder("""
            SELECT id, code, name, district_code, annexure
            FROM hed_institution
            WHERE active = true
            """);

        java.util.List<Object> params = new java.util.ArrayList<>();

        // If NGC is explicitly clicked from the College branch, retrieve both types
        if (finalSubType.equals("NGC")) {
            sql.append(" AND sub_type_code IN ('NGC_488', 'NGC_662') ");
        } else {
            sql.append(" AND sub_type_code = ? ");
            params.add(finalSubType);
        }

        java.util.List<String> rdeDistricts = new java.util.ArrayList<>();
        if (subType.startsWith("RDE_BBS")) {
            rdeDistricts = java.util.Arrays.asList("ANGUL", "CUTTACK", "DHENKANAL", "JAJPUR", "KHURDA", "NAYAGARH", "PURI", "JAGATSINGHPUR", "KENDRAPARA");
        } else if (subType.startsWith("RDE_BAL")) {
            rdeDistricts = java.util.Arrays.asList("BALASORE", "KEONJHAR", "MAYURBHANJ", "BHADRAK");
        } else if (subType.startsWith("RDE_BER")) {
            rdeDistricts = java.util.Arrays.asList("BOUDH", "GAJAPATI", "GANJAM", "KANDHAMAL");
        } else if (subType.startsWith("RDE_SBP")) {
            rdeDistricts = java.util.Arrays.asList("BARGARH", "BALANGIR", "DEOGARH", "KALAHANDI", "NUAPADA", "SAMBALPUR", "SUBARNAPUR", "SUNDARGARH", "JHARSUGUDA");
        } else if (subType.startsWith("RDE_JEY")) {
            rdeDistricts = java.util.Arrays.asList("KORAPUT", "MALKANGIRI", "NABARANGPUR", "RAYAGADA");
        }

        if (!rdeDistricts.isEmpty()) {
            sql.append(" AND district_code IN (");
            for (int i = 0; i < rdeDistricts.size(); i++) {
                sql.append("?");
                if (i < rdeDistricts.size() - 1) sql.append(",");
            }
            sql.append(") ");
            params.addAll(rdeDistricts);
        }

        if (district != null && !district.isBlank()) {
            sql.append(" AND district_code = ? ");
            params.add(district);
        }

        if (query != null && !query.isBlank()) {
            sql.append(" AND name ILIKE ? ");
            params.add("%" + query + "%");
        }

        sql.append(" ORDER BY name LIMIT ? ");
        params.add(limit);

        return ResponseEntity.ok(jdbc.queryForList(sql.toString(), params.toArray()));
    }
}