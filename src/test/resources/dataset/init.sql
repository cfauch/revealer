create table horcrux_users (
id bigint auto_increment not null primary key,
name varchar (120) not null,
profile varchar(80),
mail varchar(80)
);

insert into horcrux_users (name, profile, mail) values
('cfauch', 'adminitsrateur', null),
('casper', 'ghost', 'casper@yolo.com'),
('radj', 'guest', 'radj@yolo.com'),
('silvester', 'guest', null);
