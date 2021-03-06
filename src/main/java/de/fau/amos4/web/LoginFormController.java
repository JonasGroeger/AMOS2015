/**
 * Personalfragebogen 2.0. Revolutionize form data entry for taxation and
 * other purposes.
 * Copyright (C) 2015 Attila Bujaki, Werner Sembach, Jonas Gröger, Oswaldo
 * Bejarano, Ardhi Sutadi, Nikitha Mohan, Benedikt Rauh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fau.amos4.web;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.fau.amos4.model.Client;
import de.fau.amos4.model.fields.Title;
import de.fau.amos4.service.ClientRepository;
import de.fau.amos4.service.ClientService;
import de.fau.amos4.util.EmailSender;

@Controller
public class LoginFormController
{
    private final ClientRepository clientRepository;

    @Autowired
    public LoginFormController(ClientRepository clientRepository, ClientService clientService)
    {
        this.clientRepository = clientRepository;
    }

    @RequestMapping("/client/register")
    public String ClientRegister(Model model)
    {
        // Create a client object for the currently registered client
        Client NewClient = new Client();
        model.addAttribute("client", NewClient);
        model.addAttribute("allTitles", Title.values());
        // Display the registration page
        return "client/register";
    }

    @RequestMapping("/client/submit")
    public String ClientSubmit(HttpServletRequest request, @ModelAttribute(value = "client") Client client)
            throws MessagingException
    {
        // Generate new confirmation string for the client
        client.generateConfirmationString();
        // Set client to inactive
        client.setActivated(false);
        // Save new, in-activate client
        clientRepository.save(client);

        // Prepare and send email
        String contextPath = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getServletPath().replace("/client/submit", "/client/confirm");
        String ConfirmationCode = client.getConfirmationString();
        // TODO: Replace this with Thymeleaf based tample generated content
        String Content = "<a href='" + contextPath + "?id=" + client.getId() + "&confirmation=" + ConfirmationCode + "'>Confirm my email address.</a>";
        EmailSender sender = new EmailSender();
        sender.SendEmail(client.getEmail(), "Personalragebogen 2.0 - Confirmation", Content, null, null);

        // Display login screen after
        return "redirect:/?m=registered";
    }

    @RequestMapping("/client/confirm")
    public String ClientConfirm(@RequestParam(value = "id", required = true) long clientId,
                                @RequestParam(value = "confirmation", required = true) String enteredConfirmationCode)
            throws MessagingException
    {
        Client client = this.clientRepository.findOne(clientId);
        if (client.tryToActivate(enteredConfirmationCode)) {
            // Save client after successful activation
            this.clientRepository.save(client);
            return "redirect:/?m=confirmed";
        } else {
            return "redirect:/?m=confirmfail";
        }
    }
}
