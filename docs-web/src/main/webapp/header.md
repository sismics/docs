The web client and Android application for **Teedy** are only examples
of what is possible with the provided REST API. Everything you see in those apps are
accessible using the API.

This documentation is divided in two parts. The first will get you started on essentials
steps like authentication and the second part is a full reference of every endpoints.

## API URL
The base URL depends on your server. If your instance of Teedy is accessible through
`https://teedy.mycompany.com`, then the base API URL is `https://teedy.mycompany.com/api`.

## Verbs and status codes
The API uses RESTful verbs.

| Verb | Description |
|---|---|
| `GET` | Select one or more items |
| `PUT` | Create a new item |
| `POST` | Update an item |
| `DELETE` | Delete an item |

Successful calls return a HTTP code 200, anything else if an error.

## Dates
All dates are returned in UNIX timestamp format in milliseconds.

## Authentication
#### **Step 1: [POST /user/login](#api-User-PostUserLogin)**

A call to this endpoint will return a cookie header. Here is a CURL example:
```
curl -i -X POST -d username=admin -d password=admin https://docs.mycompany.com/api/user/login
Set-Cookie: auth_token=64085630-2ae6-415c-9a92-4b22c107eaa4
```

#### **Step 2: Authenticated API calls**

All following API calls must have a cookie header supplying the given token. Here is a CURL example:
```
curl -i -X GET -H "Cookie: auth_token=64085630-2ae6-415c-9a92-4b22c107eaa4" https://docs.mycompany.com/api/document/list
{"total":12,"documents":[...]}
```

#### **Step 3: [POST /user/logout](#api-User-PostUserLogout)**

A call to this API with a given `auth_token` cookie will make it unusable for other calls.
```
curl -i -X POST -H "Cookie: auth_token=64085630-2ae6-415c-9a92-4b22c107eaa4" https://docs.mycompany.com/api/user/logout
```

## Document search syntax

The `/api/document/list` endpoint use a String `search` parameter.

This parameter is split in segments using the space character (the other whitespace characters are not considered).

If a segment contains exactly one colon (`:`), it will used as a field criteria (see bellow).
In other cases (zero or more than one colon), the segment will be used as a search criteria for all fields including the document's files content.

### Search fields

If a search `VALUE` is considered invalid, the search result will be empty.

* Content
  * `full:VALUE`: `VALUE` is used as search criteria for all fields, including the document's files content
  * `simple:VALUE`: `VALUE` is used as a search criteria for all fields except the document's files content
* Date
  * `after:VALUE`: the document must have been created after or at the `VALUE` moment, accepted format are `yyyy`, `yyyy-MM` and `yyyy-MM-dd`
  * `at:VALUE`: the document must have been created at the `VALUE` moment, accepted format are `yyyy`, `yyyy-MM` and `yyyy-MM-dd` (for `yyyy` it must be the same year, for `yyyy-MM` the same month, for `yyyy-MM-dd` the same day)
  * `before:VALUE`: the document must have been created before or at the `VALUE` moment, accepted format are `yyyy`, `yyyy-MM` and `yyyy-MM-dd`
  * `uafter:VALUE`: the document must have been last updated after or at the `VALUE` moment, accepted format are `yyyy`, `yyyy-MM` and `yyyy-MM-dd`
  * `at:VALUE`: the document must have been updated at the `VALUE` moment, accepted format are `yyyy`, `yyyy-MM` and `yyyy-MM-dd` (for `yyyy` it must be the same year, for `yyyy-MM` the same month, for `yyyy-MM-dd` the same day)
  * `ubefore:VALUE`: the document must have been updated before or at the `VALUE` moment, accepted format are `yyyy`, `yyyy-MM` and `yyyy-MM-dd`
* Language
  * `lang:VALUE`: the document must be of the specified language (example: `en`)
* Mime
  * `mime:VALUE`: the document must be of the specified mime type (example: `image/png`)
* Shared
  * `shared:VALUE`: if `VALUE` is `yes`the document must be shared, for other `VALUE`s the criteria is ignored
* Tags: several `tags` or `!tag:` can be specified and the document must match all filters
  * `tag:VALUE`: the document must contain a tag or a child of a tag that starts with `VALUE`, case is ignored
  * `!tag:VALUE`: the document must not contain a tag or a child of a tag that starts with `VALUE`, case is ignored
* Titles: several `title` can be specified, and the document must match any of the titles
  * `title:VALUE`: the title of the document must be `VALUE`
* User
  * `by:VALUE`: the document creator's username must be `VALUE` with an exact match, the user must not be deleted
* Workflow
  * `workflow:VALUE`: if `VALUE` is `me` the document must have an active route, for other `VALUE`s the criteria is ignored
