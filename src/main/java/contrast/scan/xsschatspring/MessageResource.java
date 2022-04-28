package contrast.scan.xsschatspring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@RestController
public class MessageResource {

    @Autowired
    EntityManager entityManager;

    @GetMapping(value = "/messages/{room}", produces = "application/json")
    @Transactional
    public ChatMessage messages(@PathVariable String room) {
        TypedQuery<String> query = entityManager.createQuery("select me.content from MessageEntity me",String.class);
        List<String> msgs = query.getResultList(); // source, tagged db-source
        ChatMessage result = new ChatMessage();
        result.room = room; // Sanitized: StringEscapeUtils.escapeHtml4(room);
        if (msgs.size() > 0) {
            result.msgs = String.join("<br />", msgs);  // Sanitized: StringEscapeUtils.escapeHtml4( String.join("<br />", msgs););
        } else {
            result.msgs += "<i>No messages</i>";
        }
        return result;
    }


    /**
     * Variant:
     * - Reflected XSS via Room-Name
     * - Reflected XSS via msg ... but that's not convincing, because hardly anybody XSS'es himself
     */

    @PostMapping("/messages")
    @Transactional
    public void post(@RequestBody  String msg) {
        var stmt = Instant.now().toString();
        MessageEntity m = new MessageEntity();
        m.setContent(stmt + " - " + msg);
        entityManager.persist(m);
    }

}