package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
//        Optional<CashCard> cashCard = Optional.ofNullable(cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
        CashCard cashCard = findCashCard(requestedId, principal);
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard cashCard, UriComponentsBuilder uriBuilder, Principal principal) {
        CashCard createdCashCard = cashCardRepository.save(new CashCard(null, cashCard.amount(), principal.getName()));
        URI locationOfNewCashCard = uriBuilder
                .path("cashcards/{id}")
                .buildAndExpand(createdCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @GetMapping
    private ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(
                principal.getName(), PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);
        if (cashCard != null) {
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal) {
        if (cashCardRepository.existsByIdAndOwner(requestedId, principal.getName())) {
            cashCardRepository.deleteById(requestedId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
