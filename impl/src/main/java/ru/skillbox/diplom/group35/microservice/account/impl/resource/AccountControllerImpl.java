package ru.skillbox.diplom.group35.microservice.account.impl.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import ru.skillbox.diplom.group35.library.core.annotation.EnableExceptionHandler;
import ru.skillbox.diplom.group35.microservice.account.api.dto.AccountDto;
import ru.skillbox.diplom.group35.microservice.account.api.dto.AccountSearchDto;
import ru.skillbox.diplom.group35.microservice.account.impl.service.AccountService;
import ru.skillbox.diplom.group35.microservice.account.api.resource.AccountController;

import java.util.UUID;

/**
 * AccountControllerImpl
 *
 * @author Georgii Vinogradov
 */

@Slf4j
@RestController
@EnableExceptionHandler
@RequiredArgsConstructor
public class AccountControllerImpl implements AccountController {
    private final AccountService accountService;

    @Override
    public ResponseEntity<AccountDto> get(@RequestHeader("Authorization") String token) {
        log.info("call method get with token: {}", token);
        return ResponseEntity.ok(accountService.get(new AccountDto()));
    }

    @Override
    public ResponseEntity<AccountDto> getById(@PathVariable(name = "id") UUID id) {
        log.info("call getById with id: {}", id);
        return ResponseEntity.ok(accountService.getById(id));
    }

    @Override
    public ResponseEntity<Page<AccountDto>> search(AccountSearchDto searchDto, Pageable pageable) {
        log.info("call search");
        return ResponseEntity.ok(accountService.search(searchDto, pageable));
    }

    @Override
    public ResponseEntity<Page<AccountDto>> getAll(AccountSearchDto searchDto, Pageable page) {
        log.info("call getAll");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public ResponseEntity<AccountDto> create(@RequestBody AccountDto dto) {
        log.info("call create");
        return ResponseEntity.ok(accountService.create(dto));
    }

    @Override
    public ResponseEntity<AccountDto> update(@RequestHeader("Authorization") String token) {
        log.info("call update with token: {}", token);
        return ResponseEntity.ok(accountService.update(new AccountDto()));
    }

    @Override
    public ResponseEntity<AccountDto> update(@RequestBody AccountDto dto) {
        log.info("call update");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public void delete(@RequestHeader("Authorization") String token) {
        log.info("call delete with token: {}", token);
        accountService.delete(new AccountDto());
    }

    @Override
    public void deleteById(@PathVariable(name = "id") UUID id) {
        log.info("call delete with id: {}", id);
        accountService.deleteById(id);
    }
}
