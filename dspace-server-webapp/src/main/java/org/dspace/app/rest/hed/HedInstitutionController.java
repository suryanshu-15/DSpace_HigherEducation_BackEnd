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
            @RequestParam String branch) {

        String sql = """
            SELECT code, label, group_name, has_children, display_order
            FROM hed_section_type
            WHERE branch_code = ?
              AND active = true
            ORDER BY display_order
            """;
        return ResponseEntity.ok(jdbc.queryForList(sql, branch));
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

        StringBuilder sql = new StringBuilder("""
            SELECT id, code, name, district_code, annexure
            FROM hed_institution
            WHERE sub_type_code = ?
              AND active = true
            """);

        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(subType);

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