---
layout: default
---

<div>
{% for post in site.posts %}
<a href="{{ post.url }}"><h2> {{ post.title }} </h2></a>

{{ post.content | strip_html | truncatewords: 40 }}

<em>Posted on {{ post.date | date_to_string }}</em>

{% endfor %}
</div>