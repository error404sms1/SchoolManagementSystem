package SchoolManagementSystem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@RestController
public class SmsAssignmentController {
    private final JdbcTemplate jdbc;
    private final SmsAccessService access;
    private final Path root;

    public SmsAssignmentController(JdbcTemplate jdbc, SmsAccessService access,
            @Value("${sms.upload.dir:${user.home}/sms-private-uploads}") String uploadDirectory) {
        this.jdbc=jdbc;
        this.access=access;
        this.root=Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @PostMapping("/api/teacher/assignment-links")
    public Map<String,String> addLink(@RequestBody SmsDtos.NewAssignment value) {
        access.requireScope(value.gradeNumber(),value.subjectId());
        String url=validateLink(value.linkUrl());
        jdbc.update("INSERT INTO sms_teacher_assignment(title,description,grade_number,subject_id," +
            "link_url,uploaded_by) VALUES(?,?,?,?,?,?)",
            SmsAdminController.required(value.title(),"Title"),value.description(),value.gradeNumber(),
            value.subjectId(),url,access.username());
        return Map.of("message","Assignment link recorded");
    }

    @PostMapping(value="/api/teacher/assignments",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,String> upload(@RequestParam String title,
            @RequestParam(required=false) String description,
            @RequestParam int gradeNumber,
            @RequestParam String subjectId,
            @RequestParam("file") MultipartFile file) throws IOException {
        access.requireScope(gradeNumber,subjectId);
        if(file==null||file.isEmpty()||file.getSize()>10_000_000L)
            throw new IllegalArgumentException("File required; maximum size is 10 MB");
        String original=Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        original=Paths.get(original.replace('\\','/')).getFileName().toString();
        String lower=original.toLowerCase(Locale.ROOT);
        if(!(lower.endsWith(".pdf")||lower.endsWith(".docx")||lower.endsWith(".txt")||
                lower.endsWith(".png")||lower.endsWith(".jpg")||lower.endsWith(".jpeg")))
            throw new IllegalArgumentException("Only PDF, DOCX, TXT, PNG and JPG files are allowed");
        Files.createDirectories(root);
        String stored=UUID.randomUUID().toString()+lower.substring(lower.lastIndexOf('.'));
        Path destination=root.resolve(stored).normalize();
        if(!destination.getParent().equals(root)) throw new IllegalArgumentException("Invalid filename");
        try (java.io.InputStream stream = file.getInputStream()) {
            Files.copy(stream, destination);
        }
        try {
            jdbc.update("INSERT INTO sms_teacher_assignment(title,description,grade_number,subject_id," +
                "stored_file,original_filename,content_type,uploaded_by) VALUES(?,?,?,?,?,?,?,?)",
                SmsAdminController.required(title,"Title"),description,gradeNumber,subjectId,
                stored,original,MediaType.APPLICATION_OCTET_STREAM_VALUE,access.username());
        } catch(RuntimeException e) {
            Files.deleteIfExists(destination);
            throw e;
        }
        return Map.of("message","Assignment file uploaded");
    }

    @GetMapping("/api/teacher/assignments")
    public List<Map<String,Object>> teacherAssignments(@RequestParam int grade,@RequestParam String subjectId) {
        access.requireScope(grade,subjectId);
        return jdbc.queryForList("SELECT id,title,description,grade_number,subject_id,link_url," +
            "original_filename,uploaded_by,created_at FROM sms_teacher_assignment " +
            "WHERE grade_number=? AND subject_id=? ORDER BY created_at DESC",grade,subjectId);
    }

    @GetMapping("/api/admin/assignments")
    public List<Map<String,Object>> allAssignments() {
        return jdbc.queryForList("SELECT id,title,description,grade_number,subject_id,link_url," +
            "original_filename,uploaded_by,created_at FROM sms_teacher_assignment ORDER BY created_at DESC");
    }

    @GetMapping({"/api/admin/assignments/{id}/download","/api/teacher/assignments/{id}/download"})
    public ResponseEntity<Resource> download(@PathVariable long id) throws IOException {
        List<Map<String,Object>> rows=jdbc.queryForList(
            "SELECT grade_number,subject_id,stored_file,original_filename FROM sms_teacher_assignment WHERE id=?",id);
        if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Assignment not found");
        Map<String,Object> r=rows.get(0);
        int grade=((Number)r.get("grade_number")).intValue();
        access.requireScope(grade,(String)r.get("subject_id"));
        String filename=(String)r.get("stored_file");
        if(filename==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "This assignment is a link; open the link_url instead");
        Path p=root.resolve(filename).normalize();
        if(!p.getParent().equals(root) || !Files.isRegularFile(p))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Attachment file missing");
        Resource resource=new UrlResource(p.toUri());
        String original=(String)r.get("original_filename");
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(original,StandardCharsets.UTF_8).build().toString())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(Files.size(p))
            .body(resource);
    }

    private static String validateLink(String link) {
        try {
            URI u=URI.create(SmsAdminController.required(link,"Assignment link"));
            if(!("https".equalsIgnoreCase(u.getScheme())||"http".equalsIgnoreCase(u.getScheme())) ||
                u.getHost()==null) throw new IllegalArgumentException("Use a valid HTTPS or HTTP assignment link");
            return u.toString();
        } catch(java.lang.IllegalArgumentException e) {
            throw new IllegalArgumentException("Use a valid HTTPS or HTTP assignment link");
        }
    }
}
