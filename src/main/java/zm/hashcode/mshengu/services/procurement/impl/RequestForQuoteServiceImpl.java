/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zm.hashcode.mshengu.services.procurement.impl;

import com.google.gwt.thirdparty.guava.common.collect.ImmutableList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import zm.hashcode.mshengu.domain.procurement.RequestForQuote;
import zm.hashcode.mshengu.repository.procurement.RequestForQuoteRepository;
import zm.hashcode.mshengu.services.procurement.RequestForQuoteService;

/**
 *
 * @author Luckbliss
 */
@Service
public class RequestForQuoteServiceImpl implements RequestForQuoteService {

    @Autowired
    private RequestForQuoteRepository repository;

    @Override
    @Cacheable("requestForQuotes")
    public List<RequestForQuote> findAll() {
        return ImmutableList.copyOf(repository.findAll());
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "requestForQuotes", allEntries = true)})
    public void persist(RequestForQuote request) {
        repository.save(request);

    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "requestForQuotes", allEntries = true)})
    public void merge(RequestForQuote request) {
        if (request.getId() != null) {
            repository.save(request);
        }
    }

    @Override
    public RequestForQuote findById(String id) {
        try {
            return repository.findOne(id);
        } catch (IllegalArgumentException iaEx) {
            return null;
        }
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "requestForQuotes", allEntries = true)})
    public void delete(RequestForQuote request) {
        repository.delete(request);
    }

    @Override
    public RequestForQuote findByRfqNumber(String rfqNumber) {
        return repository.findByRfqNumber(rfqNumber);
    }
}
