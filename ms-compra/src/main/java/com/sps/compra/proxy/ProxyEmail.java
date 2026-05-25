package com.sps.compra.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.logging.Logger;

@Component
public class ProxyEmail {

    private static final Logger logger = Logger.getLogger(ProxyEmail.class.getName());

    private final JavaMailSender mailSender;

    @Value("${app.compra.base.url}")
    private String compraBaseUrl;

    @Value("${spring.mail.username:noreply@sps.com}")
    private String fromEmail;

    public ProxyEmail(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoAprobacion(String correoCliente, String nombreCliente,
                                       Long numeroCompra, BigDecimal precioTotal) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(correoCliente);
            message.setSubject("SPS - Planes Aprobados - Compra #" + numeroCompra);

            String urlPago = compraBaseUrl + "/ws/compra/" + numeroCompra + "/pago";
            String body = "Estimado(a) " + nombreCliente + ",\n\n"
                    + "Sus planes de salud han sido aprobados por el SNS.\n\n"
                    + "Compra #: " + numeroCompra + "\n"
                    + "Total a pagar: $" + precioTotal + "\n\n"
                    + "Para completar su compra, realice el pago en el siguiente enlace:\n"
                    + urlPago + "\n\n"
                    + "Gracias por elegir SPS.\n"
                    + "Equipo SPS";

            message.setText(body);
            mailSender.send(message);
            logger.info("ProxyEmail: correo de aprobacion enviado a " + correoCliente + " para compra " + numeroCompra);
        } catch (Exception e) {
            logger.severe("ProxyEmail.enviarCorreoAprobacion error para compra " + numeroCompra + ": " + e.getMessage());
        }
    }

    public void enviarCorreoConfirmacion(String correoCliente, String nombreCliente,
                                         Long numeroCompra, BigDecimal precioTotal) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(correoCliente);
            message.setSubject("SPS - Compra Confirmada - #" + numeroCompra);

            String body = "Estimado(a) " + nombreCliente + ",\n\n"
                    + "Su pago ha sido procesado exitosamente.\n\n"
                    + "Compra #: " + numeroCompra + "\n"
                    + "Total pagado: $" + precioTotal + "\n\n"
                    + "Sus planes de salud han quedado activos en el sistema.\n"
                    + "Bienvenido(a) a SPS. Sus planes de salud ya están disponibles.\n\n"
                    + "Gracias por elegir SPS.\n"
                    + "Equipo SPS";

            message.setText(body);
            mailSender.send(message);
            logger.info("ProxyEmail: correo de confirmacion enviado a " + correoCliente + " para compra " + numeroCompra);
        } catch (Exception e) {
            logger.severe("ProxyEmail.enviarCorreoConfirmacion error para compra " + numeroCompra + ": " + e.getMessage());
        }
    }
}
