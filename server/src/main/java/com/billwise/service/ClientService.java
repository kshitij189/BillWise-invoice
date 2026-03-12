package com.billwise.service;

import com.billwise.model.Client;
import com.billwise.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Page<Client> getClientsPaginated(String userId, int page) {
        Pageable pageable = PageRequest.of(page - 1, 8, Sort.by(Sort.Direction.DESC, "createdAt"));
        return clientRepository.findByUserId(userId, pageable);
    }

    public List<Client> getClientsByUser(String searchQuery) {
        return clientRepository.findByUserId(searchQuery);
    }

    public Client getClientById(String id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
    }

    public Client createClient(Client client) {
        client.setCreatedAt(new Date());
        return clientRepository.save(client);
    }

    public Client updateClient(String id, Client clientUpdate) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

        if (clientUpdate.getName() != null) existing.setName(clientUpdate.getName());
        if (clientUpdate.getEmail() != null) existing.setEmail(clientUpdate.getEmail());
        if (clientUpdate.getPhone() != null) existing.setPhone(clientUpdate.getPhone());
        if (clientUpdate.getAddress() != null) existing.setAddress(clientUpdate.getAddress());
        if (clientUpdate.getUserId() != null) existing.setUserId(clientUpdate.getUserId());

        return clientRepository.save(existing);
    }

    public void deleteClient(String id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }
}
