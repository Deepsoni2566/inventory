// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract CategoryManager {
    struct Category {
        uint256 id;
        string name;
        string description;
        bool isDeleted;
        uint256 timestamp;
    }

    mapping(uint256 => Category) public categories;
    uint256 public categoryCount;

    event CategoryAdded(uint256 id, string name, string description);
    event CategoryUpdated(uint256 id, string name, string description);
    event CategoryRemoved(uint256 id);
    event CategoryRestored(uint256 id);

    function addCategory(string memory name, string memory description) public returns (uint256) {
        categoryCount++;
        categories[categoryCount] = Category(
            categoryCount,
            name,
            description,
            false,
            block.timestamp
        );
        emit CategoryAdded(categoryCount, name, description);
        return categoryCount;
    }

    function updateCategory(uint256 id, string memory newName, string memory newDescription) public {
        require(id > 0 && id <= categoryCount, "Invalid category ID");
        require(!categories[id].isDeleted, "Category is deleted");

        categories[id].name = newName;
        categories[id].description = newDescription;
        categories[id].timestamp = block.timestamp;

        emit CategoryUpdated(id, newName, newDescription);
    }

    function removeCategory(uint256 id) public {
        require(id > 0 && id <= categoryCount, "Invalid category ID");
        require(!categories[id].isDeleted, "Category already deleted");

        categories[id].isDeleted = true;
        categories[id].timestamp = block.timestamp;

        emit CategoryRemoved(id);
    }

    function restoreCategory(uint256 id) public {
        require(id > 0 && id <= categoryCount, "Invalid category ID");
        require(categories[id].isDeleted, "Category is not deleted");

        categories[id].isDeleted = false;
        categories[id].timestamp = block.timestamp;

        emit CategoryRestored(id);
    }

    function getCategory(uint256 id) public view returns (
        uint256, string memory, string memory, bool, uint256
    ) {
        require(id > 0 && id <= categoryCount, "Invalid category ID");
        Category memory cat = categories[id];
        return (cat.id, cat.name, cat.description, cat.isDeleted, cat.timestamp);
    }

    function getAllCategories() public view returns (
        uint256[] memory,
        string[] memory,
        string[] memory,
        bool[] memory,
        uint256[] memory
    ) {
        uint256[] memory ids = new uint256[](categoryCount);
        string[] memory names = new string[](categoryCount);
        string[] memory descriptions = new string[](categoryCount);
        bool[] memory deletedFlags = new bool[](categoryCount);
        uint256[] memory timestamps = new uint256[](categoryCount);

        for (uint256 i = 1; i <= categoryCount; i++) {
            Category memory cat = categories[i];
            ids[i-1] = cat.id;
            names[i-1] = cat.name;
            descriptions[i-1] = cat.description;
            deletedFlags[i-1] = cat.isDeleted;
            timestamps[i-1] = cat.timestamp;
        }

        return (ids, names, descriptions, deletedFlags, timestamps);
    }
}
