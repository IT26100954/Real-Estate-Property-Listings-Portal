CREATE TABLE IF NOT EXISTS `users` (
  `id` VARCHAR(64) PRIMARY KEY,
  `username` VARCHAR(254) NOT NULL,
  `password` VARCHAR(512) NOT NULL,
  `email` VARCHAR(254) NOT NULL,
  `phone` VARCHAR(254) NOT NULL,
  `userType` VARCHAR(254) NOT NULL,
  CONSTRAINT uq_users_username UNIQUE (`username`)
);

CREATE TABLE IF NOT EXISTS `admins` (
  `id` VARCHAR(64) PRIMARY KEY,
  `username` VARCHAR(254) NOT NULL,
  `password` VARCHAR(512) NOT NULL,
  `email` VARCHAR(254) NOT NULL,
  `adminLevel` VARCHAR(254) NOT NULL,
  CONSTRAINT uq_admins_username UNIQUE (`username`)
);

CREATE TABLE IF NOT EXISTS `agents` (
  `id` VARCHAR(64) PRIMARY KEY,
  `fullName` VARCHAR(254) NOT NULL,
  `email` VARCHAR(254) NOT NULL,
  `phone` VARCHAR(254) NOT NULL,
  `licenseNo` VARCHAR(254) NOT NULL,
  `city` VARCHAR(254) NOT NULL,
  `experienceYears` INT NOT NULL,
  `agentType` VARCHAR(254) NOT NULL,
  `commissionRate` DECIMAL(20,4) NOT NULL,
  CONSTRAINT uq_agents_email UNIQUE (`email`),
  CONSTRAINT uq_agents_licenseNo UNIQUE (`licenseNo`)
);

CREATE TABLE IF NOT EXISTS `properties` (
  `id` VARCHAR(64) PRIMARY KEY,
  `title` VARCHAR(254) NOT NULL,
  `propertyType` VARCHAR(254) NOT NULL,
  `listingType` VARCHAR(254) NOT NULL,
  `city` VARCHAR(254) NOT NULL,
  `address` VARCHAR(2000) NOT NULL,
  `price` DECIMAL(20,4) NOT NULL,
  `bedrooms` INT NOT NULL,
  `bathrooms` INT NOT NULL,
  `areaSqft` DECIMAL(20,4) NOT NULL,
  `ownerId` VARCHAR(64) NOT NULL,
  `agentId` VARCHAR(64) NOT NULL,
  `status` VARCHAR(254) NOT NULL,
  `extraValue` DECIMAL(20,4) NOT NULL,
  CONSTRAINT fk_properties_ownerId FOREIGN KEY (`ownerId`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT fk_properties_agentId FOREIGN KEY (`agentId`) REFERENCES `agents` (`id`) ON DELETE RESTRICT,
  CONSTRAINT ck_property_amount CHECK (`price` > 0 AND `areaSqft` > 0)
);

CREATE TABLE IF NOT EXISTS `appointments` (
  `id` VARCHAR(64) PRIMARY KEY,
  `propertyId` VARCHAR(64) NOT NULL,
  `buyerId` VARCHAR(64) NOT NULL,
  `agentId` VARCHAR(64) NOT NULL,
  `viewDate` DATE NOT NULL,
  `viewTime` TIME NOT NULL,
  `viewType` VARCHAR(254) NOT NULL,
  `status` VARCHAR(254) NOT NULL,
  `note` VARCHAR(2000) NOT NULL,
  CONSTRAINT fk_appointments_propertyId FOREIGN KEY (`propertyId`) REFERENCES `properties` (`id`) ON DELETE RESTRICT,
  CONSTRAINT fk_appointments_buyerId FOREIGN KEY (`buyerId`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT fk_appointments_agentId FOREIGN KEY (`agentId`) REFERENCES `agents` (`id`) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS `offers` (
  `id` VARCHAR(64) PRIMARY KEY,
  `propertyId` VARCHAR(64) NOT NULL,
  `buyerId` VARCHAR(64) NOT NULL,
  `offerType` VARCHAR(254) NOT NULL,
  `offerAmount` DECIMAL(20,4) NOT NULL,
  `durationMonths` INT NOT NULL,
  `offerDate` DATE NOT NULL,
  `status` VARCHAR(254) NOT NULL,
  CONSTRAINT fk_offers_propertyId FOREIGN KEY (`propertyId`) REFERENCES `properties` (`id`) ON DELETE RESTRICT,
  CONSTRAINT fk_offers_buyerId FOREIGN KEY (`buyerId`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT ck_offer_amount CHECK (`offerAmount` > 0)
);

CREATE TABLE IF NOT EXISTS `reviews` (
  `id` VARCHAR(64) PRIMARY KEY,
  `reviewerId` VARCHAR(64) NOT NULL,
  `targetId` VARCHAR(64) NOT NULL,
  `reviewType` VARCHAR(254) NOT NULL,
  `rating` INT NOT NULL,
  `comment` VARCHAR(300) NOT NULL,
  `reviewDate` DATE NOT NULL,
  CONSTRAINT fk_reviews_reviewerId FOREIGN KEY (`reviewerId`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT ck_reviews_rating CHECK (`rating` BETWEEN 1 AND 5)
);

CREATE TABLE IF NOT EXISTS activity_log (logId VARCHAR(64) PRIMARY KEY, actorId VARCHAR(64) NOT NULL, action VARCHAR(2000) NOT NULL, dateTime TIMESTAMP NOT NULL);
