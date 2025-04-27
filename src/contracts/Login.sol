// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract Login {
    event LoginAttempt(address indexed userAddress, string email, bool success, uint timestamp);

    struct Attempt {
        string email;
        bool success;
        uint timestamp;
    }

    Attempt[] public attempts;

    function logAttempt(string memory email, bool success) public {
        attempts.push(Attempt({
            email: email,
            success: success,
            timestamp: block.timestamp
        }));
        emit LoginAttempt(msg.sender, email, success, block.timestamp);
    }

    function getAttempt(uint index) public view returns (string memory, bool, uint) {
        require(index < attempts.length, "Invalid index");
        Attempt memory attempt = attempts[index];
        return (attempt.email, attempt.success, attempt.timestamp);
    }

    function getTotalAttempts() public view returns (uint) {
        return attempts.length;
    }
}
