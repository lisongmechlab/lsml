# LSML Link Trampoline
What is a link trampoline? LSML defines a new URI scheme called `lsml://` according to RFC3986. The LSML windows installer registers a URI handler for the `lsml://` schema so that when such a link is opened in windows, LSML is launched.

So far so good. However, being a rather unknown URI scheme and most web forums only accept a subset of common schemes (think http, https, ftp, mailto, etc) that are transformed to hyperlinks that can be opened by the system URI handler. So if someone posts a link like: `lsml://rwAEEBYKHg4gDBYKHhWSpSnGUIykBKR06AfVOtR06BKT` on a forum it won't become a clickable hyperlink that will open in LSML. The then requires the user to copy-paste the link into the LSML import dialogue, not a smooth experience.

Allright, get to it already, what the hecc is a link trampoline? A link trampoline is a tiny web service that takes a HTTP link and 301 redirects the user to the `lsml://` protocol. So a a trampoline link would look something like this: http://t.li-soft.org/?l=rwAEEBYKHg4gDBYKHhWSpSnGUIykBKR06AfVOtR06BKT  the HTTP GET parameter `l` contains the path of the LSML URI schema and the webpage `t.li-soft.org/(index.php)` is the trampoline that "bounces" the user to the correct URI, hence a trampoline.

In other words, LSML would encode a loadout to it's interal compressed, Base64 encoded format, HTML encode it and generate a link to the trampoline service with a http(s) schema, the trampoline immediately redirects the user to the `lsml://` URI scheme which is opened by the OS.

## How to deploy?
The trapoline service is deployed using GCP free tier as an AppEngine PHP app in `europe-west3` using the LSML GCP project.

```
gcloud config set project <PROJECT_ID>
cd lsml/link-trampoline
gcloud app deploy
```

The base URL is https://li-soft.ey.r.appspot.com/ and a DNS entry maps it to `t.li-soft.org`.