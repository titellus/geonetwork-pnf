Ce dépôt contient l'instance GeoNetwork personnalisée pour les
Parc nationaux de France. Cette instance foncionne en mode
multinodes (cf. https://github.com/geonetwork/core-geonetwork/wiki/Multinodes-mode)
avec un noeud par parc.


# Personnalisation du catalogue

* Configuration des langues par défaut
* Configuration du fond de carte (cf. apps/search/js/map/Settings.js)
* Les logos des parcs sont ajoutés
* Les thésaurus suivants ont été ajoutés :
 * Contours des parcs
 * Communes des parcs
 * Départements
 * Régions
 * Thèmes INSPIRE
 * GEMET (http://www.eionet.europa.eu/gemet/en/about/)
* La liste des langues par défaut a été réduite.
* La liste des trustedHost a été mise à jour
* Configuration du service CSW de découverte


Pour PNF, préconfiguration du moissonnage des fiches des autres parcs.


# Installater le catalogue

* Installer Java 7
* Installer Tomcat 7
* Installer le catalogue
 * Option 1 : Télécharger le WAR de l'application
 * Option 2 : Compiler l'application (pré-requis git et maven doivent être installé)
```
git clone --recursive https://github.com/titellus/geonetwork-pnf
cd geonetwork-pnf
git checkout -b stable-develop
mvn clean install -DskipTests
cp web/target/geonetwork.war $CATALINA_HOME/webapps/catalogue.war
```
* Configurer Tomcat
 * Définir l'encodage dans le fichier conf/server.xml
```
<Connector ... URIEncoding="UTF-8">
```
 * Définir le répertoire des données
```
export CATALOGUE_DIR=/app/tomcat
export JAVA_OPTS="$JAVA_OPTS -Xms1g -Xmx2g -XX:MaxPermSize=512m \
              -Dgeonetwork.dir=$CATALOGUE_DIR/data/ \
              -Dgeonetwork.schema.dir=$CATALOGUE_DIR/webapps/catalogue/WEB-INF/data/config/schema_plugins \
              -Dgeonetwork.resources.dir=$CATALOGUE_DIR/webapps/catalogue \
              -Dgeonetwork.codeList.dir=$CATALOGUE_DIR/webapps/catalogue/WEB-INF/data/config/codelist"
```
* Importer ou créer les bases de données


# Lancer le catalogue

Démarrer Tomcat pour lancer le catalogue.

Lors du premier lancement, la structure des bases de données est crée avec les données
par défaut. Chaque noeud est initialisé avec un compte admin/admin. Il est recommandé
de :
* changer le mot de passe du compte admin (cf. http://extranet.parcnational.fr/catalogue/pnf/fre/admin.console#/organization/users)
* modifier le nom du catalogue (cf. http://extranet.parcnational.fr/catalogue/pnf/fre/admin.console#/settings/system)
* modifier le logo du catalogue (cf. http://extranet.parcnational.fr/catalogue/pnf/fre/admin.console#/settings/logo)


# Importer des fiches existantes

S'il est nécessaire d'ajouter des fiches existantes au catalogue, le plus simple
est de copier les fiches XML dans un répertoire du serveur et d'utiliser l'import
en série (cf. http://extranet.parcnational.fr/catalogue/pnp/eng/catalog.edit#/import).

Si les fiches importées doivent être publique, faire une recherche, sélectionner l'ensemble
des fiches puis modifier les privilèges pour rendre les fiches accessibles à tous.

# Ajouter un nouveau noeud


* Créer la base de données du noeud
```
CREATE DATABASE catalogue_pnf
  WITH ENCODING='UTF8'
       OWNER="www-data"
       CONNECTION LIMIT=-1;
```
* Créer la configuration du noeud
 * option 1 : utiliser le script de création
```
cd WEB-INF/node-utils
./node-mgr.sh www-data www-data pnf jdbc:postgresql://localhost:5432/catalogue_pnf postgres
```
 * option 2 : configuration manuelle
  * Ajouter un fichier portant l'identifiant du noeud dans WEB-INF/config-node
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="             http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd             http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd"
       default-lazy-init="true">
  <import resource="classpath*:/config-spring-geonetwork.xml"/>
  <import resource="../config-db/database_migration.xml"/>
  <context:property-override properties-ref="pnf-configuration-overrides"/>

  <!-- Définir l'identifiant du noeud et
  indiquer si le noeud est celui par défaut (un seul noeud
  peut être celui par défaut). -->
  <bean class="org.fao.geonet.NodeInfo" id="nodeInfo">
    <property value="pnf" name="id"/>
    <property value="true" name="defaultNode"/>
  </bean>


  <bean class="org.springframework.beans.factory.config.PropertiesFactoryBean" id="pnf-configuration-overrides">
    <property name="properties">
      <props>
        <!-- Configurer ici la connexion à la
        base de données. -->
        <prop key="jdbcDataSource.username">www-data</prop>
        <prop key="jdbcDataSource.password">www-data</prop>
        <prop key="jdbcDataSource.maxActive">10</prop>
        <prop key="jdbcDataSource.maxIdle">2</prop>
        <prop key="jdbcDataSource.initialSize">0</prop>
        <prop key="jdbcDataSource.Url">jdbc:postgresql://localhost:5432/catalogue_pnf</prop>
      </props>
    </property>
  </bean>

  <import resource="../config-db/postgres.xml"/>
</beans>
```
  * WEB-INF/web.xml : Ajouter dans la webapp le servlet-mapping pour le noeud
```
  <servlet-mapping>
    <servlet-name>gn-servlet</servlet-name>
    <url-pattern>/pnf/*</url-pattern>
  </servlet-mapping>
```


# Sauvegarder l'application

* Sauvegarder les bases de données

```
for db in pnf pnc pne png pag pnm pnpc pnp pnrun pnv pncal
do
  pg_dump --host localhost --port 5432 --username "postgres" --no-password  \
  --format plain --no-owner --encoding UTF8 --no-privileges --verbose \
  --file "/tmp/catalogue_$db.sql" "catalogue_$db"
done
```

* Sauvegarder les répertoires des données de chaque instance ($CATALOGUE_DIR/data/)
* Sauvegarder le WAR (si modification)



# Lancer l'application à partir du codesource

Si besoin, il est possible de lancer l'application à partir des sources de la
manière suivante :

```
cd web
export CATALOGUE_DIR=/home/francois/Workspace/github/pnf-geonetwork/web/src/main
export MAVEN_OPTS="-Xms1g -Xmx2g -XX:MaxPermSize=512m \
              -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 \
              -Dgeonetwork.dir=$CATALOGUE_DIR/data/ \
              -Dgeonetwork.schema.dir=$CATALOGUE_DIR/webapp/WEB-INF/data/config/schema_plugins \
              -Dgeonetwork.resources.dir=$CATALOGUE_DIR/webapp \
              -Dgeonetwork.codeList.dir=$CATALOGUE_DIR/webapp/WEB-INF/data/config/codelist"
mvn jetty:run -Penv-dev
```


# Divers


## Pour lancer des fonctions SQL sur chaque base :

```

for db in `psql -qAt -U www-data -c "SELECT datname FROM pg_database WHERE datname like 'catalogue_%' AND datistemplate = false;" postgres` ;
do
  psql -U www-data -c "SELECT value FROM settings WHERE name = 'system/platform/version'" $db ;
done

or


for db in `psql -qAt -U www-data -c "SELECT datname FROM pg_database WHERE datname like 'catalogue_%' AND datistemplate = false;" postgres` ;
do
  psql -U www-data -f tmp.sql $db ;
done
```
