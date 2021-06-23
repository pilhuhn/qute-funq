create table qtemplate(id int, bae varchar , type varchar, subtype varchar, body varchar);
grant all on qtemplate to PUBLIC;
insert into qtemplate values (1,'my-bundle:my-app:a_type', 'instant_mail', 'body', '<h1>Hello {key1} and {key2}</h2>');
