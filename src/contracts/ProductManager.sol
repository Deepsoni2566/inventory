// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract ProductManager {
    struct Product {
        string name;
        string category;
        string description;
        uint256 quantity;
        uint256 price;
        uint256 vat;
        bool isDeleted;
    }

    mapping(uint256 => Product) public products;
    uint256 public productCount;

    event ProductAdded(uint256 id, string name);
    event ProductUpdated(uint256 id);
    event ProductDeleted(uint256 id);

    function addProduct(string memory name, string memory category, string memory description, uint256 quantity, uint256 price, uint256 vat) public {
        products[productCount] = Product(name, category, description, quantity, price, vat, false);
        emit ProductAdded(productCount, name);
        productCount++;
    }

    function updateProduct(uint256 id, string memory name, string memory category, string memory description, uint256 quantity, uint256 price) public {
        Product storage p = products[id];
        p.name = name;
        p.category = category;
        p.description = description;
        p.quantity = quantity;
        p.price = price;
        emit ProductUpdated(id);
    }

    function deleteProduct(uint256 id) public {
        products[id].isDeleted = true;
        emit ProductDeleted(id);
    }

    function getProduct(uint256 id) public view returns (string memory, string memory, string memory, uint256, uint256, uint256) {
        Product memory p = products[id];
        return (p.name, p.category, p.description, p.quantity, p.price, p.vat);
    }
}
