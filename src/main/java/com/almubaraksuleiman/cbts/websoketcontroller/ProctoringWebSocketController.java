package com.almubaraksuleiman.cbts.websoketcontroller;//package com.almubaraksuleiman.cbts.websoketcontroller;
//
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.stereotype.Controller;
//
//// ProctoringWebSocketController.java
//@Controller
//public class ProctoringWebSocketController {
//
//    private final ProctoringSessionService proctoringService;
//
//    public ProctoringWebSocketController(ProctoringSessionService proctoringService) {
//        this.proctoringService = proctoringService;
//    }
//
//    @MessageMapping("/proctoring/heartbeat")
//    public void handleHeartbeat(HeartbeatDTO heartbeat) {
//        proctoringService.updateHeartbeat(heartbeat);
//    }
//
//    @MessageMapping("/proctoring/violation")
//    public void handleViolation(ViolationDTO violation) {
//        proctoringService.recordViolation(violation);
//    }
//
//    @MessageMapping("/proctoring/media-status")
//    public void handleMediaStatus(MediaStatusDTO mediaStatus) {
//        // Update media status in monitoring data
//    }
//
//    @MessageMapping("/proctoring/session-control")
//    public void handleSessionControl(SessionControlDTO control) {
//        if (control.getAction() == SessionAction.TERMINATE) {
//            proctoringService.endProctoringSession(control.getSessionId(), control.getReason());
//        }
//        // Broadcast to specific candidate
//        // messagingTemplate.convertAndSend("/topic/session/" + control.getSessionId(), control);
//    }
//}