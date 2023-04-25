package ru.skillbox.diplom.group35.microservice.account.impl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.skillbox.diplom.group35.library.core.utils.SecurityUtil;
import ru.skillbox.diplom.group35.microservice.account.api.dto.*;
import ru.skillbox.diplom.group35.microservice.account.domain.model.Account;
import ru.skillbox.diplom.group35.microservice.account.domain.model.Account_;
import ru.skillbox.diplom.group35.microservice.account.impl.mapper.AccountMapper;
import ru.skillbox.diplom.group35.microservice.account.impl.repository.AccountRepository;
import ru.skillbox.diplom.group35.microservice.friend.feignclient.FriendFeignClient;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.skillbox.diplom.group35.library.core.utils.SpecificationUtil.*;

/**
 * service
 *
 * @author Denis_Kholmogorov
 */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final SecurityUtil securityUtil;

    private final FriendFeignClient friendFeignClient;

    public AccountStatisticResponseDto getAccountCount(AccountStatisticRequestDto statisticRequestDto) {
        List<AccountCountPerAge> countPerAges = accountRepository.equalOrLessThen(statisticRequestDto.getDate());
        List<StatPerMonth> statisticPerMonthList = accountRepository.getStatPerMonth(
                statisticRequestDto.getFirstMonth(), statisticRequestDto.getLastMonth().plusMonths(1));
        List<StatisticPerDateDto> statisticPerDateDtoList = statisticPerMonthList.stream()
                .map(accountMapper::mapToStatisticPerDateDto)
                .collect(Collectors.toList());
        return new AccountStatisticResponseDto(countPerAges, statisticPerDateDtoList);
    }


    public Page<AccountDto> search(AccountSearchDto searchDto, Pageable pageable) {
        List<UUID> blockedByIdsList = friendFeignClient.getBlockFriendId().getBody();
        log.info("got a list of blocked users");
        blockedByIdsList.add(securityUtil.getAccountDetails().getId());
        searchDto.setBlockedByIds(blockedByIdsList);

        Page<Account> accounts = accountRepository.findAll(getSpecByAllFields(searchDto), pageable);
        Page<AccountDto> accountDtoPage = accounts.map(accountMapper::mapToDto);

        List<UUID> ids = accountDtoPage.stream()
                .map(dto -> dto.getId())
                .collect(Collectors.toList());

        try {
            ResponseEntity<Map<UUID, String>> response = friendFeignClient.checkFriend(ids);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                Map<UUID, String> foundFriendStatus = response.getBody();
                accountDtoPage.stream()
                        .filter(dto -> foundFriendStatus.containsKey(dto.getId()))
                        .forEach(dto -> {
                            StatusCode statusCode = StatusCode.valueOf(foundFriendStatus.get(dto.getId()));
                            dto.setStatusCode(statusCode);
                            log.info("set new status id:{} from:{} to:{}", dto.getId(), dto.getStatusCode(), statusCode);
                        });
            }
        } catch (FeignException e) {
            log.info("friend service unavailable");
        }

        return accountDtoPage;
    }

    public AccountDto get() {
        return getById(securityUtil.getAccountDetails().getId());
    }

    public AccountDto getById(UUID id) {
        return accountMapper.mapToDto(accountRepository.getById(id));
    }

    public AccountDto getByEmail(String email) {
        Account account = accountRepository
                .findOne(getSpecByEmail(email))
                .orElseThrow(EntityNotFoundException::new);
        return accountMapper.mapToDtoWithPass(account);
    }

    public AccountDto create(AccountDto dto) {
        Account account = accountRepository.save(accountMapper.mapToAccount(dto));
        return accountMapper.mapToDto(account);
    }

    public AccountDto update(AccountDto accountDto) {
        Account extractAccount = accountRepository.getById(securityUtil.getAccountDetails().getId());
        Account updatedAccount = accountMapper.updateAccount(accountDto, extractAccount);
        return accountMapper.mapToDto(accountRepository.save(updatedAccount));
    }

    public void delete() {
        accountRepository.deleteById(securityUtil.getAccountDetails().getId());
    }

    public void deleteById(UUID id) {
        accountRepository.deleteById(id);
    }

    public static Specification<Account> getSpecByAllFields(AccountSearchDto searchDto) {
        return getBaseSpecification(searchDto)
                .and(in(Account_.id, searchDto.getIds(), true))
                .and(notIn(Account_.id, searchDto.getBlockedByIds(), true))
                .and(equal(Account_.firstName, searchDto.getFirstName(), true))
                .and(equal(Account_.lastName, searchDto.getLastName(), true))
                .and(likeLowerCase(Account_.firstName, searchDto.getFirstName(), true))
                .and(likeLowerCase(Account_.lastName, searchDto.getLastName(), true))
                .and(between(Account_.birthDate,
                        searchDto.getAgeTo() == null ? null : ZonedDateTime.now().minusYears(searchDto.getAgeTo()),
                        searchDto.getAgeFrom() == null ? null : ZonedDateTime.now().minusYears(searchDto.getAgeFrom()),
                        true));
    }

    private static Specification<Account> getSpecByEmail(String email) {
        return (root, query, cb) -> cb.equal(root.get("email"), email);
    }
}
