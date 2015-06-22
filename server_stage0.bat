for /f %%a in (torcs_directory.txt) do (cd %%a)
wtorcs.exe -nofuel -nodamage -nolaptime -r config\raceman\stage0.xml