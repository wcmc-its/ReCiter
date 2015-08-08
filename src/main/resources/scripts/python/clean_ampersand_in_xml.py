
# Replace & with &amp; in xml files.

import os
replacement = " &amp; "
path = "../../data/pubmed/aaledo/"
for dname, dirs, files in os.walk(path):
    for fname in files:
        fpath = os.path.join(dname, fname)
        with open(fpath) as f:
            s = f.read()
        s = s.replace(" & ", replacement)
        with open(fpath, "w") as f:
            f.write(s)