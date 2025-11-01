package com.maintenance.repository;

import com.maintenance.entity.Attachment;
import com.maintenance.entity.Ticket;
import com.maintenance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    List<Attachment> findByTicketOrderByUploadedAtDesc(Ticket ticket);
    
    List<Attachment> findByUploadedByOrderByUploadedAtDesc(User uploadedBy);
    
    @Query("SELECT a FROM Attachment a WHERE a.ticket = :ticket AND a.fileType LIKE 'image/%'")
    List<Attachment> findImagesByTicket(@Param("ticket") Ticket ticket);
    
    @Query("SELECT SUM(a.fileSize) FROM Attachment a WHERE a.ticket = :ticket")
    Long getTotalFileSizeByTicket(@Param("ticket") Ticket ticket);
}
