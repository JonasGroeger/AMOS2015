package de.fau.amos4.web;

import de.fau.amos4.model.Client;
import de.fau.amos4.service.ClientRepository;
import de.fau.amos4.util.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginFormController
{
    private final ClientRepository clientRepository;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    public LoginFormController(ClientRepository clientRepository)
    {
        this.clientRepository = clientRepository;
    }

    @RequestMapping("/client/register")
    public String RegisterClient(HttpServletRequest request, @RequestParam(value = "client", required = true) Client client) throws AddressException, MessagingException
    {
        // Generate new confirmation string for the client
        client.generateConfirmationString();
        // Set client to inactive
        client.setActivated(false);
        // Save new, unactivated client
        clientRepository.save(client);

        // Prepare and send email
        String contextPath = servletContext.getContextPath();
        String ConfirmationCode = client.getConfirmationString();
        String Content = "<a href=" + contextPath + "/client/confirm?id=" + client.getId() + "&confirmation=" + ConfirmationCode + ">";
        EmailSender sender = new EmailSender();
        sender.SendEmail(client.getEmail(), "Personalragebogen 2.0 - Confirmation", Content);

        // Display login screen after
        return "RegistrationAlmostReady";
    }

    @RequestMapping("/client/confirm")
    public String RegisterClient(@RequestParam(value = "id", required = true) long clientId, @RequestParam(value = "confirmation", required = true) String enteredConfirmationCode) throws AddressException, MessagingException
    {
        Client client = this.clientRepository.findOne(clientId);
        if (client.tryToActivate(enteredConfirmationCode)) {
            // Save client after successful activation
            this.clientRepository.save(client);
        }

        return "redirect:/client/login";
    }
}
