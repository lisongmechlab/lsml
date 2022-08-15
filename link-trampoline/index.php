<?php
$link = "lsml://".str_replace("lsml://", "", $_GET['l']);
header("HTTP/1.1 301 Moved Permanently");
header("Location: ".$link);
?>
