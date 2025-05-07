INSERT INTO manufacturers (id, name) VALUES
('m001', 'Alpha Motors'),
('m002', 'Beta Cars'),
('m003', 'Gamma Auto'),
('m004', 'Delta Vehicles');

INSERT INTO categories (id, name) VALUES
('c001', 'Compact'),
('c002', 'Luxury'),
('c003', 'Convertible'),
('c004', 'Jeep');

INSERT INTO cars (id, production_year, model, manufacturer_id) VALUES
('car001', 2020, 'Zeta', 'm001'),
('car002', 2021, 'Delta', 'm002'),
('car003', 2019, 'Epsilon', 'm003'),
('car004', 2022, 'Theta', 'm001'),
('car005', 2023, 'Kappa', 'm002');

INSERT INTO cars_categories (car_id, category_id) VALUES
('car001', 'c001'),
('car001', 'c002'),
('car002', 'c002'),
('car003', 'c003'),
('car004', 'c001'),
('car005', 'c002');

